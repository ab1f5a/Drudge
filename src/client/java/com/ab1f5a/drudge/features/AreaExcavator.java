package com.ab1f5a.drudge.features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.ai.PathFinder;
import com.ab1f5a.drudge.ai.PathProcessor;
import com.ab1f5a.drudge.events.GUIRenderListener;
import com.ab1f5a.drudge.events.RenderListener;
import com.ab1f5a.drudge.events.UpdateListener;
import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.settings.EnumSetting;
import com.ab1f5a.drudge.settings.SliderSetting;
import com.ab1f5a.drudge.settings.SliderSetting.ValueDisplay;
import com.ab1f5a.drudge.util.BlockBreaker;
import com.ab1f5a.drudge.util.BlockUtils;
import com.ab1f5a.drudge.util.OverlayRenderer;
import com.ab1f5a.drudge.util.RenderUtils;
import com.ab1f5a.drudge.util.RotationUtils;

public final class AreaExcavator extends Feature
	implements UpdateListener, RenderListener, GUIRenderListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 2, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final EnumSetting<Mode> mode =
		new EnumSetting<>("Mode", Mode.values(), Mode.FAST);
	
	private final OverlayRenderer overlay = new OverlayRenderer();
	
	private Step step;
	private BlockPos posLookingAt;
	private Area area;
	private BlockPos currentBlock;
	private ExcavatorPathFinder pathFinder;
	private PathProcessor processor;

	private final HashSet<BlockPos> remainingPositions = new HashSet<>();
	private final ArrayList<AABB> remainingBoxes = new ArrayList<>();

	private static final Direction[] SIDES = Direction.values();
	
	public AreaExcavator()
	{
		super("AreaExcavator");
		addSetting(range);
		addSetting(mode);
	}
	
	@Override
	public Component getRenderName()
	{
		if(step != Step.EXCAVATE || area == null)
			return getDisplayName();

		return Component.translatable(
			"gui.drudge.feature.areaexcavator.progress",
			getProgressPercentage() + "%");
	}

	private void showActionBar()
	{
		if(area == null)
			return;

		// 同 TreeFeller：每 tick 重发，物品栏上方的提示才不会淡出。
		MC.gui.setOverlayMessage(Component.translatable(
			"gui.drudge.hud.areaexcavator", getProgressPercentage() + "%"),
			false);
	}

	private int getProgressPercentage()
	{
		int totalBlocks = area.blocksList.size();
		if(totalBlocks == 0)
			return 0;

		return (int)((totalBlocks - area.remainingBlocks)
			/ (double)totalBlocks * 100);
	}
	
	@Override
	protected void onEnable()
	{
		// TunnelBorer 是唯一会与本功能抢玩家控制权的功能，二者互斥。
		DRUDGE.getModules().tunnelBorer.setEnabled(false);

		step = Step.START_POS;
		remainingPositions.clear();
		remainingBoxes.clear();

		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
		EVENTS.add(GUIRenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		EVENTS.remove(GUIRenderListener.class, this);

		MC.gui.setOverlayMessage(Component.empty(), false);

		for(Step step : Step.values())
			step.pos = null;
		posLookingAt = null;
		area = null;
		remainingPositions.clear();
		remainingBoxes.clear();

		MC.gameMode.stopDestroyBlock();
		overlay.resetProgress();
		currentBlock = null;
		
		pathFinder = null;
		processor = null;
		PathProcessor.releaseControls();
	}
	
	@Override
	public void onUpdate()
	{
		if(step.selectPos)
			handlePositionSelection();
		else if(step == Step.SCAN_AREA)
			scanArea();
		else if(step == Step.EXCAVATE)
		{
			excavate();
			showActionBar();
		}
	}
	
	@Override
	public void onRender(PoseStack matrixStack, float partialTicks)
	{
		if(pathFinder != null)
			pathFinder.renderPath(matrixStack, true);
		
		int black = 0x80000000;
		int gray = 0x26404040;
		int green1 = 0x2600FF00;

		if(area != null)
		{
			if(!remainingBoxes.isEmpty())
			{
				RenderUtils.drawOutlinedBoxes(matrixStack, remainingBoxes, black,
					true);
				RenderUtils.drawSolidBoxes(matrixStack, remainingBoxes, green1,
					true);
			}

			AABB areaBox = new AABB(area.minX, area.minY, area.minZ,
				area.minX + area.sizeX, area.minY + area.sizeY,
				area.minZ + area.sizeZ).deflate(1 / 16.0);
			RenderUtils.drawOutlinedBox(matrixStack, areaBox, black, true);
		}
		
		if(area == null && step == Step.END_POS && step.pos != null)
		{
			AABB preview = AABB
				.encapsulatingFullBlocks(Step.START_POS.pos, Step.END_POS.pos)
				.deflate(1 / 16.0);
			RenderUtils.drawOutlinedBox(matrixStack, preview, black, true);
		}
		
		ArrayList<AABB> selectedBoxes = new ArrayList<>();
		for(Step step : Step.SELECT_POSITION_STEPS)
			if(step.pos != null)
				selectedBoxes.add(new AABB(step.pos).deflate(1 / 16.0));
		RenderUtils.drawOutlinedBoxes(matrixStack, selectedBoxes, black, false);
		RenderUtils.drawSolidBoxes(matrixStack, selectedBoxes, green1, false);
		
		if(posLookingAt != null)
		{
			AABB box = new AABB(posLookingAt).deflate(1 / 16.0);
			RenderUtils.drawOutlinedBox(matrixStack, box, black, false);
			RenderUtils.drawSolidBox(matrixStack, box, gray, false);
		}
		
		overlay.render(matrixStack, partialTicks, currentBlock);
	}
	
	@Override
	public void onRenderGUI(GuiGraphicsExtractor context, float partialTicks)
	{
		if(step == Step.EXCAVATE)
			return;

		Component message = step.selectPos && step.pos != null
			? Component.translatable("gui.drudge.feature.areaexcavator.confirm")
			: step.getDisplayMessage();

		Font tr = MC.font;
		int msgWidth = tr.width(message);

		int msgX1 = context.guiWidth() / 2 - msgWidth / 2;
		int msgX2 = msgX1 + msgWidth + 2;
		int msgY1 = context.guiHeight() / 2 + 1;
		int msgY2 = msgY1 + 10;

		context.fill(msgX1, msgY1, msgX2, msgY2, 0x80000000);

		context.text(tr, message, msgX1 + 2, msgY1 + 1, CommonColors.WHITE,
			false);
	}
	
	public void enableWithArea(BlockPos pos1, BlockPos pos2)
	{
		setEnabled(true);
		Step.START_POS.pos = pos1;
		Step.END_POS.pos = pos2;
		step = Step.SCAN_AREA;
	}
	
	private void handlePositionSelection()
	{
		if(step.pos != null
			&& InputConstants.isKeyDown(MC.getWindow(), GLFW.GLFW_KEY_ENTER))
		{
			step = Step.values()[step.ordinal() + 1];
			
			if(!step.selectPos)
				posLookingAt = null;
			
			return;
		}
		
		if(MC.hitResult instanceof BlockHitResult)
		{
			posLookingAt = ((BlockHitResult)MC.hitResult).getBlockPos();
			
			if(MC.options.keyShift.isDown())
				posLookingAt = posLookingAt
					.relative(((BlockHitResult)MC.hitResult).getDirection());
			
		}else
			posLookingAt = null;
		
		if(posLookingAt != null && MC.options.keyUse.isDown())
			step.pos = posLookingAt;
	}
	
	private void scanArea()
	{
		if(area == null)
		{
			area = new Area(Step.START_POS.pos, Step.END_POS.pos);
			Step.START_POS.pos = null;
			Step.END_POS.pos = null;
		}
		
		for(int i = 0; i < area.scanSpeed && area.iterator.hasNext(); i++)
		{
			BlockPos pos = area.iterator.next();

			if(BlockUtils.canBeClicked(pos))
			{
				area.blocksList.add(pos);
				area.blocksSet.add(pos);
			}
		}

		if(!area.iterator.hasNext())
		{
			area.remainingBlocks = area.blocksList.size();
			step = Step.values()[step.ordinal() + 1];
		}
	}

	private void updateRemainingBlocks()
	{
		remainingPositions.clear();
		remainingBoxes.clear();

		// 每 tick 重算一次而非每帧：两遍都要读世界，逐帧做太贵。
		// instabuild 提前读出——过滤跑在 worker 线程上，不能碰 player。
		boolean instabuild = MC.player.getAbilities().instabuild;
		remainingPositions.addAll(area.blocksList.parallelStream()
			.filter(pos -> canStillBeBroken(pos, instabuild)).toList());

		area.remainingBlocks = remainingPositions.size();

		// 六个邻居都还在的方块从外面看不到，不用画框；只画剩余方块的外壳，
		// 几千块的大区域才能缩到几百个框。
		for(BlockPos pos : remainingPositions)
			if(!isEnclosed(pos))
				remainingBoxes.add(new AABB(pos).inflate(0.005));
	}

	private boolean canStillBeBroken(BlockPos pos, boolean instabuild)
	{
		if(!BlockUtils.canBeClicked(pos))
			return false;

		return instabuild || !BlockUtils.isUnbreakable(pos);
	}

	private boolean isEnclosed(BlockPos pos)
	{
		for(int i = 0; i < SIDES.length; i++)
			if(!remainingPositions.contains(pos.relative(SIDES[i])))
				return false;

		return true;
	}
	
	private void excavate()
	{
		Vec3 eyesVec = RotationUtils.getEyesPos();
		Comparator<BlockPos> cNextTargetBlock =
			Comparator.<BlockPos> comparingInt(BlockPos::getY).reversed()
				.thenComparingDouble(pos -> pos.distToCenterSqr(eyesVec));
		
		ArrayList<BlockPos> validBlocks = getValidBlocks();
		validBlocks.sort(cNextTargetBlock);
		currentBlock = null;
		
		boolean legit = mode.getSelected() == Mode.LEGIT;
		if(MC.player.getAbilities().instabuild && !legit)
		{
			MC.gameMode.stopDestroyBlock();
			overlay.resetProgress();
			
			for(BlockPos pos : validBlocks)
			{
				currentBlock = pos;
				break;
			}
			
			BlockBreaker.breakBlocksWithPacketSpam(validBlocks);
			
		}else
		{
			for(BlockPos pos : validBlocks)
			{
				DRUDGE.getModules().autoToolSwitch.equipIfEnabled(pos);
				if(!BlockBreaker.breakOneBlock(pos))
					continue;
				
				currentBlock = pos;
				break;
			}
			
			if(currentBlock == null)
			{
				MC.gameMode.stopDestroyBlock();
				overlay.resetProgress();
			}
		}
		
		overlay.updateProgress();
		
		updateRemainingBlocks();

		if(area.remainingBlocks == 0)
		{
			setEnabled(false);
			return;
		}

		if(pathFinder == null)
		{
			BlockPos closestBlock =
				remainingPositions.stream().min(cNextTargetBlock).get();

			pathFinder = new ExcavatorPathFinder(closestBlock);
		}
		
		if(!pathFinder.isDone() && !pathFinder.isFailed())
		{
			PathProcessor.lockControls();
			
			pathFinder.think();
			
			if(!pathFinder.isDone() && !pathFinder.isFailed())
				return;
			
			pathFinder.formatPath();
			
			processor = pathFinder.getProcessor();
		}
		
		if(processor != null
			&& !pathFinder.isPathStillValid(processor.getIndex()))
		{
			pathFinder = new ExcavatorPathFinder(pathFinder);
			return;
		}
		
		processor.process();
		
		if(processor.isDone())
		{
			pathFinder = null;
			processor = null;
			PathProcessor.releaseControls();
		}
	}
	
	private ArrayList<BlockPos> getValidBlocks()
	{
		Vec3 eyesVec = RotationUtils.getEyesPos();
		BlockPos eyesBlock = BlockPos.containing(eyesVec);
		double rangeSq = Math.pow(range.getValue() + 0.5, 2);
		int blockRange = range.getValueCeil();
		
		return BlockUtils.getAllInBoxStream(eyesBlock, blockRange)
			.filter(pos -> pos.distToCenterSqr(eyesVec) <= rangeSq)
			.filter(area.blocksSet::contains).filter(BlockUtils::canBeClicked)
			.filter(pos -> !BlockUtils.isUnbreakable(pos))
			.sorted(
				Comparator.comparingDouble(pos -> pos.distToCenterSqr(eyesVec)))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private static enum Mode
	{
		FAST("Fast"),
		
		LEGIT("Legit");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private static enum Step
	{
		START_POS("gui.drudge.feature.areaexcavator.step.start_pos", true),

		END_POS("gui.drudge.feature.areaexcavator.step.end_pos", true),

		SCAN_AREA("gui.drudge.feature.areaexcavator.step.scan_area", false),

		EXCAVATE("gui.drudge.feature.areaexcavator.step.excavate", false);

		private static final Step[] SELECT_POSITION_STEPS =
			{START_POS, END_POS};

		private final String messageKey;
		private boolean selectPos;

		private BlockPos pos;

		private Component message;
		private Language messageLanguage;

		private Step(String messageKey, boolean selectPos)
		{
			this.messageKey = messageKey;
			this.selectPos = selectPos;
		}

		private Component getDisplayMessage()
		{
			// 以 Language.getInstance() 的实例身份作为缓存失效条件，
			// 语言真正切换时才重建，避免每帧分配新 Component。
			Language language = Language.getInstance();

			if(message == null || messageLanguage != language)
			{
				message = Component.translatable(messageKey);
				messageLanguage = language;
			}

			return message;
		}
	}
	
	private static class Area
	{
		private final int minX, minY, minZ;
		private final int sizeX, sizeY, sizeZ;
		
		private final int totalBlocks, scanSpeed;
		private final Iterator<BlockPos> iterator;
		
		private int remainingBlocks;
		
		private final ArrayList<BlockPos> blocksList = new ArrayList<>();
		private final HashSet<BlockPos> blocksSet = new HashSet<>();
		
		private Area(BlockPos start, BlockPos end)
		{
			int startX = start.getX();
			int startY = start.getY();
			int startZ = start.getZ();
			
			int endX = end.getX();
			int endY = end.getY();
			int endZ = end.getZ();
			
			minX = Math.min(startX, endX);
			minY = Math.min(startY, endY);
			minZ = Math.min(startZ, endZ);
			
			sizeX = Math.abs(startX - endX);
			sizeY = Math.abs(startY - endY);
			sizeZ = Math.abs(startZ - endZ);
			
			totalBlocks = (sizeX + 1) * (sizeY + 1) * (sizeZ + 1);
			scanSpeed = Mth.clamp(totalBlocks / 30, 1, 16384);
			iterator = BlockUtils.getAllInBox(start, end).iterator();
		}
	}
	
	private static class ExcavatorPathFinder extends PathFinder
	{
		public ExcavatorPathFinder(BlockPos goal)
		{
			super(goal);
			setThinkTime(10);
		}
		
		public ExcavatorPathFinder(ExcavatorPathFinder pathFinder)
		{
			super(pathFinder);
		}
		
		@Override
		protected boolean checkDone()
		{
			BlockPos goal = getGoal();
			
			return done = goal.below(2).equals(current)
				|| goal.above().equals(current) || goal.north().equals(current)
				|| goal.south().equals(current) || goal.west().equals(current)
				|| goal.east().equals(current)
				|| goal.below().north().equals(current)
				|| goal.below().south().equals(current)
				|| goal.below().west().equals(current)
				|| goal.below().east().equals(current);
		}
	}
}
