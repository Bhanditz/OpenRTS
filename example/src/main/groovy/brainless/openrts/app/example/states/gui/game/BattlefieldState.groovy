package brainless.openrts.app.example.states.gui.game;

import java.util.logging.Logger

import model.ModelManager
import model.battlefield.army.ArmyManager
import model.battlefield.army.components.Unit
import view.EditorView
import view.camera.Camera
import view.camera.IsometricCamera
import view.math.TranslateUtil
import brainless.openrts.app.example.GameInputInterpreter
import brainless.openrts.app.example.states.AppStateCommon;
import brainless.openrts.event.BattleFieldUpdateEvent;
import brainless.openrts.event.EventManager;

import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.google.inject.Injector
import com.jme3.app.state.AppStateManager
import com.jme3.input.InputManager

import controller.CommandManager
import controller.SpatialSelector
import geometry.geom2d.AlignedBoundingBox
import geometry.geom2d.Point2D
import groovy.transform.CompileStatic

@CompileStatic
public class BattlefieldState extends AppStateCommon {
	
	private static final Logger logger = Logger.getLogger(BattlefieldState.class.getName());

	private boolean paused = false;
	private Point2D zoneStart;
	
	private boolean drawingZone = false;
	@Inject
	protected EditorView view;
	
	@Inject
	protected SpatialSelector spatialSelector;
	
	protected Camera isometricCamera;
	@Inject
	protected Injector injector
	
	@Inject
	protected InputManager inputManager;
	
	@Inject
	protected com.jme3.renderer.Camera cam
	
	@Inject
	protected GameInputInterpreter inputInterpreter
	
	@Inject
	private ModelManager modelManager
	
	@Inject
	private ArmyManager armyManager
	
	@Inject
	private CommandManager commandManager
	
	@Inject
	public BattlefieldState() {
		super()
		displayName = "GameBattlefieldState";
		show = true;
		EventManager.register(this);
	}

	@Override
	public void update(float elapsedTime) {
		updateSelectionZone();
		updateContext();
		//sguiController.update();

		view.getActorManager().render()
		// update army
		if (!paused) {
			armyManager.update(elapsedTime);
		}
	}

	public void startSelectionZone() {
		zoneStart = getMouseCoord();
	}

	public void endSelectionZone() {
		zoneStart = null;
		drawingZone = false;
	}

	public boolean isDrawingZone() {
		return drawingZone;
	}

	private void updateSelectionZone() {
		if (zoneStart == null) {
			view.getGuiNode().detachAllChildren();
			return;
		}

		Point2D coord = getMouseCoord();
		if (coord.equals(zoneStart)) {
			return;
		}

		drawingZone = coord.getDistance(zoneStart) > 10;

		AlignedBoundingBox rect = new AlignedBoundingBox(zoneStart, coord);
		List<Unit> inSelection = new ArrayList<>();
		for (Unit u : armyManager.getUnits()) {
			if (rect.contains(spatialSelector.getScreenCoord(u.getPos()))) {
				inSelection.add(u);
			}
		}
		commandManager.select(inSelection);
		view.drawSelectionArea(zoneStart, coord);
	}

	private void updateContext() {
		AlignedBoundingBox screen = new AlignedBoundingBox(Point2D.ORIGIN, isometricCamera.getCamCorner());
		List<Unit> inScreen = new ArrayList<>();
		for (Unit u : armyManager.getUnits()) {
			if (screen.contains(spatialSelector.getScreenCoord(u.getPos()))) {
				inScreen.add(u);
			}
		}
		commandManager.createContextualUnities(inScreen);

	}

	@Subscribe
	public void manageEvent(BattleFieldUpdateEvent ev) {
		placeCamera();
	}

	private placeCamera() {
		((IsometricCamera)isometricCamera).move(modelManager.getBattlefield().getMap().xSize() / 2, modelManager.getBattlefield().getMap().ySize() / 2)
	}

	// TODO: See AppState.setEnabled => use it, this is a better implementation
	public void togglePause() {
		paused = !paused;
		view.getActorManager().pause(paused);
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager);
		inputManager.setCursorVisible(true);
		//guiController.activate();
		
		view.reset();
		
		if (isometricCamera == null) {
			isometricCamera = new IsometricCamera(cam, 10, modelManager);			
		}
		placeCamera();
		inputInterpreter.registerInputs(inputManager);
		isometricCamera.registerInputs(inputManager);
		isometricCamera.activate();
	}

	private Point2D getMouseCoord() {
		return TranslateUtil.toPoint2D(inputManager.getCursorPosition());
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
		super.stateDetached(stateManager);
		inputManager.setCursorVisible(false);
		inputInterpreter.unregisterInputs(inputManager);
		isometricCamera.unregisterInputs(inputManager);
		EventManager.register(this);
	}

	@Override
	public void reshape() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(float tpf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanupState() {
		// TODO Auto-generated method stub
		
	}

}