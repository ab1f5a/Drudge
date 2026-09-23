package com.ab1f5a.drudge.features;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.ai.PathFinder;
import com.ab1f5a.drudge.ai.PathPos;
import com.ab1f5a.drudge.ai.PathProcessor;
import com.ab1f5a.drudge.events.RenderListener;
import com.ab1f5a.drudge.events.UpdateListener;
import com.ab1f5a.drudge.module.DontSaveState;
import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.features.tree.Tree;
import com.ab1f5a.drudge.features.tree.TreeUtils;
import com.ab1f5a.drudge.settings.FaceTargetSetting;
import com.ab1f5a.drudge.settings.FaceTargetSetting.FaceTarget;
import com.ab1f5a.drudge.settings.SliderSetting;
import com.ab1f5a.drudge.settings.SliderSetting.ValueDisplay;
import com.ab1f5a.drudge.settings.SwingHandSetting;
import com.ab1f5a.drudge.settings.SwingHandSetting.SwingHand;
import com.ab1f5a.drudge.util.BlockBreaker;
import com.ab1f5a.drudge.util.BlockBreaker.BlockBreakingParams;
import com.ab1f5a.drudge.util.BlockUtils;
import com.ab1f5a.drudge.util.OverlayRenderer;

@DontSaveState
public final class TreeFeller extends Feature
	implements UpdateListener, RenderListener
{
	private final SliderSetting range = new SliderSetting("Range",
		"description.drudge.setting.treefeller.range", 4.5, 1, 6, 0.05,
		ValueDisplay.DECIMAL);
	
	private final FaceTargetSetting faceTarget =
		FaceTargetSetting.withoutPacketSpam(this, FaceTarget.SERVER);
	
	private final SwingHandSetting swingHand =
		new SwingHandSetting(this, SwingHand.SERVER);
	
	private TreeFinder treeFinder;
	private AngleFinder angleFinder;
	private TreeBotPathProcessor processor;
	private Tree tree;
	
	private BlockPos currentBlock;
	private final OverlayRenderer overlay = new OverlayRenderer();
	
	public TreeFeller()
	{
		super("TreeFeller");
		addSetting(range);
		addSetting(faceTarget);
		addSetting(swingHand);
	}
	
	@Override
	public Component getRenderName()
	{
		if(treeFinder != null && !treeFinder.isDone() && !treeFinder.isFailed())
			return Component
				.translatable("gui.drudge.feature.treefeller.searching");

		if(processor != null && !processor.isDone())
			return Component.translatable("gui.drudge.feature.treefeller.going");

		if(tree != null && !tree.getLogs().isEmpty())
			return Component
				.translatable("gui.drudge.feature.treefeller.chopping");

		return getDisplayName();
	}
	
	@Override
	protected void onEnable()
	{
		treeFinder = new TreeFinder();
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);

		MC.gui.setOverlayMessage(Component.empty(), false);
		PathProcessor.releaseControls();
		treeFinder = null;
		angleFinder = null;
		processor = null;
		tree = null;
		
		if(currentBlock != null)
		{
			MC.gameMode.isDestroying = true;
			MC.gameMode.stopDestroyBlock();
			currentBlock = null;
		}
		
		overlay.resetProgress();
	}
	
	@Override
	public void onUpdate()
	{
		showActionBar();

		if(treeFinder != null)
		{
			goToTree();
			return;
		}
		
		if(tree == null)
		{
			treeFinder = new TreeFinder();
			return;
		}
		
		tree.getLogs().removeIf(Predicate.not(TreeUtils::isLog));
		
		if(tree.getLogs().isEmpty())
		{
			tree = null;
			return;
		}
		
		if(angleFinder != null)
		{
			goToAngle();
			return;
		}
		
		if(breakBlocks(tree.getLogs()))
			return;
		
		if(angleFinder == null)
			angleFinder = new AngleFinder();
	}
	
	private void showActionBar()
	{
		// setOverlayMessage 会把显示计时器重置为 60 tick，因此须每 tick 重发
		// 才能让物品栏上方这条提示持续显示；false 表示白色而非彩虹色。
		MC.gui.setOverlayMessage(
			Component.translatable("gui.drudge.hud.treefeller"), false);
	}

	private void goToTree()
	{
		if(!treeFinder.isDoneOrFailed())
		{
			PathProcessor.lockControls();
			treeFinder.findPath();
			return;
		}
		
		if(processor != null && !processor.isDone())
		{
			processor.goToGoal();
			return;
		}
		
		PathProcessor.releaseControls();
		treeFinder = null;
	}
	
	private void goToAngle()
	{
		if(!angleFinder.isDone() && !angleFinder.isFailed())
		{
			PathProcessor.lockControls();
			angleFinder.findPath();
			return;
		}
		
		if(processor != null && !processor.isDone())
		{
			processor.goToGoal();
			return;
		}
		
		PathProcessor.releaseControls();
		angleFinder = null;
	}
	
	private boolean breakBlocks(ArrayList<BlockPos> blocks)
	{
		for(BlockPos pos : blocks)
			if(breakBlock(pos))
			{
				currentBlock = pos;
				return true;
			}
		
		return false;
	}
	
	private boolean breakBlock(BlockPos pos)
	{
		BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
		if(params == null || !params.lineOfSight()
			|| params.distanceSq() > range.getValueSq())
			return false;
		
		DRUDGE.getModules().autoToolSwitch.equipBestTool(pos, false, true, 0);
		
		faceTarget.face(params.hitVec());
		
		if(MC.gameMode.continueDestroyBlock(pos, params.side()))
			swingHand.swing(InteractionHand.MAIN_HAND);
		
		overlay.updateProgress();
		
		return true;
	}
	
	@Override
	public void onRender(PoseStack matrixStack, float partialTicks)
	{
		if(treeFinder != null)
			treeFinder.renderPath(matrixStack, true);

		if(angleFinder != null)
			angleFinder.renderPath(matrixStack, true);
		
		if(tree != null)
			tree.draw(matrixStack);
		
		overlay.render(matrixStack, partialTicks, currentBlock);
	}
	
	private ArrayList<BlockPos> getNeighbors(BlockPos pos)
	{
		return BlockUtils
			.getAllInBoxStream(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
			.filter(TreeUtils::isLog)
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private abstract class TreeBotPathFinder extends PathFinder
	{
		public TreeBotPathFinder(BlockPos goal)
		{
			super(goal);
		}
		
		public TreeBotPathFinder(TreeBotPathFinder pathFinder)
		{
			super(pathFinder);
		}
		
		public void findPath()
		{
			think();
			
			if(isDoneOrFailed())
			{
				formatPath();
				processor = new TreeBotPathProcessor(this);
			}
		}
		
		public boolean isDoneOrFailed()
		{
			return isDone() || isFailed();
		}
		
		public abstract void reset();
	}
	
	private class TreeBotPathProcessor
	{
		private final TreeBotPathFinder pathFinder;
		private final PathProcessor processor;
		
		public TreeBotPathProcessor(TreeBotPathFinder pathFinder)
		{
			this.pathFinder = pathFinder;
			processor = pathFinder.getProcessor();
		}
		
		public void goToGoal()
		{
			if(!pathFinder.isPathStillValid(processor.getIndex())
				|| processor.getTicksOffPath() > 20)
			{
				pathFinder.reset();
				return;
			}
			
			if(processor.canBreakBlocks() && breakBlocks(getLeavesOnPath()))
				return;
			
			processor.process();
		}
		
		private ArrayList<BlockPos> getLeavesOnPath()
		{
			List<PathPos> path = pathFinder.getPath();
			path = path.subList(processor.getIndex(), path.size());
			
			return path.stream().flatMap(pos -> Stream.of(pos, pos.above()))
				.distinct().filter(TreeUtils::isLeaves)
				.collect(Collectors.toCollection(ArrayList::new));
		}
		
		public final boolean isDone()
		{
			return processor.isDone();
		}
	}
	
	private class TreeFinder extends TreeBotPathFinder
	{
		public TreeFinder()
		{
			super(BlockPos.containing(DrudgeClient.MC.player.position()));
		}
		
		public TreeFinder(TreeBotPathFinder pathFinder)
		{
			super(pathFinder);
		}
		
		@Override
		protected boolean isMineable(BlockPos pos)
		{
			return TreeUtils.isLeaves(pos);
		}
		
		@Override
		protected boolean checkDone()
		{
			return done = isNextToTreeStump(current);
		}
		
		private boolean isNextToTreeStump(PathPos pos)
		{
			return isTreeStump(pos.north()) || isTreeStump(pos.east())
				|| isTreeStump(pos.south()) || isTreeStump(pos.west());
		}
		
		private boolean isTreeStump(BlockPos pos)
		{
			if(!TreeUtils.isLog(pos))
				return false;
			
			if(TreeUtils.isLog(pos.below()))
				return false;
			
			analyzeTree(pos);
			
			if(tree.getLogs().size() > 6)
				return false;
			
			return true;
		}
		
		private void analyzeTree(BlockPos stump)
		{
			ArrayList<BlockPos> logs = new ArrayList<>(Arrays.asList(stump));
			ArrayDeque<BlockPos> queue = new ArrayDeque<>(Arrays.asList(stump));
			
			for(int i = 0; i < 1024; i++)
			{
				if(queue.isEmpty())
					break;
				
				BlockPos current = queue.pollFirst();
				
				for(BlockPos next : getNeighbors(current))
				{
					if(logs.contains(next))
						continue;
					
					logs.add(next);
					queue.add(next);
				}
			}
			
			tree = new Tree(stump, logs);
		}
		
		@Override
		public void reset()
		{
			treeFinder = new TreeFinder(treeFinder);
		}
	}
	
	private class AngleFinder extends TreeBotPathFinder
	{
		public AngleFinder()
		{
			super(BlockPos.containing(DrudgeClient.MC.player.position()));
			setThinkSpeed(512);
			setThinkTime(1);
		}
		
		public AngleFinder(TreeBotPathFinder pathFinder)
		{
			super(pathFinder);
		}
		
		@Override
		protected boolean isMineable(BlockPos pos)
		{
			return TreeUtils.isLeaves(pos);
		}
		
		@Override
		protected boolean checkDone()
		{
			return done = hasAngle(current);
		}
		
		private boolean hasAngle(PathPos pos)
		{
			double rangeSq = range.getValueSq();
			LocalPlayer player = DrudgeClient.MC.player;
			Vec3 eyes = Vec3.atBottomCenterOf(pos).add(0,
				player.getEyeHeight(player.getPose()), 0);
			
			for(BlockPos log : tree.getLogs())
			{
				BlockBreakingParams params =
					BlockBreaker.getBlockBreakingParams(eyes, log);
				
				if(params != null && params.lineOfSight()
					&& params.distanceSq() <= rangeSq)
					return true;
			}
			
			return false;
		}
		
		@Override
		public void reset()
		{
			angleFinder = new AngleFinder(angleFinder);
		}
	}
}
