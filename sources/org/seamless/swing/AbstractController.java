package org.seamless.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
/* loaded from: classes.dex */
public class AbstractController<V extends Container> implements Controller<V> {
    private static Logger log = Logger.getLogger(AbstractController.class.getName());
    private Map<String, DefaultAction> actions;
    private Map<Class, List<EventListener>> eventListeners;
    private Controller parentController;
    private List<Controller> subControllers;
    private V view;

    public AbstractController(V view) {
        this.subControllers = new ArrayList();
        this.actions = new HashMap();
        this.eventListeners = new HashMap();
        this.view = view;
    }

    public AbstractController() {
        this.subControllers = new ArrayList();
        this.actions = new HashMap();
        this.eventListeners = new HashMap();
    }

    public AbstractController(Controller parentController) {
        this(null, parentController);
    }

    public AbstractController(V view, Controller parentController) {
        this.subControllers = new ArrayList();
        this.actions = new HashMap();
        this.eventListeners = new HashMap();
        this.view = view;
        if (parentController != null) {
            this.parentController = parentController;
            parentController.getSubControllers().add(this);
        }
    }

    @Override // org.seamless.swing.Controller
    public V getView() {
        return this.view;
    }

    @Override // org.seamless.swing.Controller
    public Controller getParentController() {
        return this.parentController;
    }

    @Override // org.seamless.swing.Controller
    public List<Controller> getSubControllers() {
        return this.subControllers;
    }

    @Override // org.seamless.swing.Controller
    public void dispose() {
        log.fine("Disposing controller");
        Iterator<Controller> it = this.subControllers.iterator();
        while (it.hasNext()) {
            Controller subcontroller = it.next();
            subcontroller.dispose();
            it.remove();
        }
    }

    @Override // org.seamless.swing.Controller
    public void registerAction(AbstractButton source, DefaultAction action) {
        source.removeActionListener(this);
        source.addActionListener(this);
        this.actions.put(source.getActionCommand(), action);
    }

    @Override // org.seamless.swing.Controller
    public void registerAction(AbstractButton source, String actionCommand, DefaultAction action) {
        source.setActionCommand(actionCommand);
        registerAction(source, action);
    }

    public void deregisterAction(String actionCommand) {
        this.actions.remove(actionCommand);
    }

    @Override // org.seamless.swing.Controller
    public void registerEventListener(Class eventClass, EventListener eventListener) {
        Logger logger = log;
        logger.fine("Registering listener: " + eventListener + " for event type: " + eventClass.getName());
        List<EventListener> listenersForEvent = this.eventListeners.get(eventClass);
        if (listenersForEvent == null) {
            listenersForEvent = new ArrayList();
        }
        listenersForEvent.add(eventListener);
        this.eventListeners.put(eventClass, listenersForEvent);
    }

    @Override // org.seamless.swing.Controller
    public void fireEvent(Event event) {
        fireEvent(event, false);
    }

    @Override // org.seamless.swing.Controller
    public void fireEventGlobal(Event event) {
        fireEvent(event, true);
    }

    @Override // org.seamless.swing.Controller
    public void fireEvent(Event event, boolean global) {
        if (!event.alreadyFired(this)) {
            log.finest("Event has not been fired already");
            if (this.eventListeners.get(event.getClass()) != null) {
                Logger logger = log;
                logger.finest("Have listeners for this type of event: " + this.eventListeners.get(event.getClass()));
                for (EventListener eventListener : this.eventListeners.get(event.getClass())) {
                    Logger logger2 = log;
                    logger2.fine("Processing event: " + event.getClass().getName() + " with listener: " + eventListener.getClass().getName());
                    eventListener.handleEvent(event);
                }
            }
            event.addFiredInController(this);
            Logger logger3 = log;
            logger3.fine("Passing event: " + event.getClass().getName() + " DOWN in the controller hierarchy");
            for (Controller subController : this.subControllers) {
                subController.fireEvent(event, global);
            }
        } else {
            log.finest("Event already fired here, ignoring...");
        }
        if (getParentController() != null && !event.alreadyFired(getParentController()) && global) {
            Logger logger4 = log;
            logger4.fine("Passing event: " + event.getClass().getName() + " UP in the controller hierarchy");
            getParentController().fireEvent(event, global);
            return;
        }
        log.finest("Event does not propagate up the tree from here");
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            AbstractButton button = (AbstractButton) actionEvent.getSource();
            String actionCommand = button.getActionCommand();
            DefaultAction action = this.actions.get(actionCommand);
            if (action != null) {
                Logger logger = log;
                logger.fine("Handling command: " + actionCommand + " with action: " + action.getClass());
                try {
                    preActionExecute();
                    log.fine("Dispatching to action for execution");
                    action.executeInController(this, actionEvent);
                    postActionExecute();
                    finalActionExecute();
                } catch (RuntimeException ex) {
                    failedActionExecute();
                    throw ex;
                } catch (Exception ex2) {
                    failedActionExecute();
                    throw new RuntimeException(ex2);
                }
            } else if (getParentController() != null) {
                log.fine("Passing action on to parent controller");
                this.parentController.actionPerformed(actionEvent);
            } else {
                throw new RuntimeException("Nobody is responsible for action command: " + actionCommand);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Action source is not an Abstractbutton: " + actionEvent);
        }
    }

    @Override // org.seamless.swing.Controller
    public void preActionExecute() {
    }

    @Override // org.seamless.swing.Controller
    public void postActionExecute() {
    }

    @Override // org.seamless.swing.Controller
    public void failedActionExecute() {
    }

    @Override // org.seamless.swing.Controller
    public void finalActionExecute() {
    }

    public void windowClosing(WindowEvent windowEvent) {
        dispose();
        getView().dispose();
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }
}
