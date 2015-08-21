package openrts.server
;

import java.util.logging.Logger

import openrts.event.ServerEvent
import openrts.server.gui.EventBox
import tonegod.gui.core.Screen

import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.jme3.app.Application
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.math.Vector2f

import event.EventManager
import event.network.NetworkEvent
import groovy.transform.CompileStatic;

@CompileStatic
class ServerControlAppState extends AbstractAppState {

	static final Logger logger = Logger.getLogger(ServerControlAppState.class.getName());
	OpenRTSServerTonegodGUI app;
	Screen screen;

	EventBox eventBox;

	@Inject
	ServerControlAppState( OpenRTSServerTonegodGUI app , Screen screen) {
		this.app = app;
		this.screen = screen;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		initControlWindow();
		EventManager.register(this);
	}

	public void initControlWindow() {
		eventBox = new EventBox(screen, "Events", new Vector2f((Float) (screen.getWidth() / 2 - 175), (Float) (screen.getHeight() / 2 - 125))) {

					@Override
					public void onClientConnected(String msg) {
						this.receiveClientConnection(msg);
					}

					@Override
					public void onEventReceived(String msg) {
						this.receiveEvent(msg);
					}
				}
		screen.addElement(eventBox);
	}

	@Override
	public void cleanup() {
		super.cleanup();

		screen.removeElement(eventBox);
		EventManager.unregister(this);
	}

	public void finalizeUserLogin() {
		// Some call to your app to unload this AppState and load the next AppState
		//app.switchToServerControlAppStates();
		logger.info("Login was pressed");
	}

	@Subscribe
	def logNetworkEvents(NetworkEvent evt) {
		eventBox.receiveEvent("receive Networkmessage:" + evt)
	}


	@Subscribe
	def logSeverEvents(ServerEvent evt) {
		eventBox.receiveClientConnection("receive Networkmessage:" + evt)
	}
}