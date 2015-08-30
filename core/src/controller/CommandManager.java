/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package controller;

import geometry.geom2d.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.EntityManager;
import model.ModelManager;
import model.battlefield.army.ArmyManager;
import model.battlefield.army.Group;
import model.battlefield.army.Unity;
import model.battlefield.army.components.Unit;
import model.battlefield.army.motion.pathfinding.FlowField;

/**
 * @author Benoît
 */
// TODO:integrate this Class into BattlefieldController and BattleFieldGUIController
public class CommandManager {

	public static List<Unit> selection = new ArrayList<>();
	private static Map<String, Unity> unitiesInContext = new HashMap<>();
	private static boolean moveAttack = false;
	private static boolean multipleSelection = false;
	private static final CommandManager instance = new CommandManager();

	private CommandManager() {
	}

	public static void setMoveAttack() {
		moveAttack = true;
	}

	public static void setMultipleSelection(boolean val) {
		multipleSelection = val;
	}

	public static void orderHold() {
		for (Unit u : selection) {
			u.ai.orderHold();
		}
	}

	// FIXME: this is the select method and not the act method, why the this method is calling act?
	public static void select(int id, Point2D pos) {
		if (pos == null) {
			return;
		}
		if (moveAttack) {
			act(id, pos);
		} else {
			if (!multipleSelection) {
				unselect();
			}
			if (!EntityManager.isValidId(id)) {
				return;
			}
			Unit u = getUnit(id);
			changeSelection(u);
		}
	}

	public static void select(List<Unit> units) {
		if (!multipleSelection) {
			unselect();
		}
		for (Unit u : units) {
			addToSelection(u);
		}
		moveAttack = false;
	}

	private static void changeSelection(Unit u) {
		if (u == null) {
			return;
		}
		if (u.selected) {
			u.selected = false;
			selection.remove(u);
		} else {
			u.selected = true;
			selection.add(u);
		}
	}

	private static void addToSelection(Unit u) {
		if (u == null) {
			return;
		}
		u.selected = true;
		if (!selection.contains(u)) {
			selection.add(u);
		}
	}

	public static void act(int id, Point2D pos) {
		if (pos == null) {
			return;
		}
		Unit target = getUnit(id);
		Group group = new Group(selection);
		
		for (Unit u : group) {
			// First the unit removed itself from its previous group
			u.group.remove(u);
			u.group = group;
		}
		if (target != null && target.faction != selection.get(0).faction) {
			orderAttack(target);
		} else {
			orderMove(pos);
		}
		moveAttack = false;
	}

	private static void orderMove(Point2D p) {
		FlowField ff = new FlowField(ModelManager.getBattlefield().getMap(), p);
		for (Unit u : selection) {
			u.getMover().setDestination(ff);
			if (moveAttack) {
				u.ai.orderMoveAttack();
			} else {
				u.ai.orderMove();
			}
		}
	}

	private static void orderAttack(Unit enemy) {
		FlowField ff = new FlowField(ModelManager.getBattlefield().getMap(), enemy.getCoord());
		for (Unit u : selection) {
			u.getMover().setDestination(ff);
			if (moveAttack) {
				// TODO moveattacking an enemy must take care of the enemy movements
				u.ai.orderMoveAttack();
			} else {
				u.ai.orderAttack(enemy);
			}
		}
	}

	private static void unselect() {
		for (Unit u : selection) {
			u.selected = false;
		}
		selection.clear();
	}

	private static Unit getUnit(int id) {
		if (EntityManager.isValidId(id)) {
			return ArmyManager.getUnit(id);
		}
		return null;
	}

	public static void selectAll() {
		unselect();
		for (Unit u : ArmyManager.getUnits()) {
			changeSelection(u);
		}
	}

	public static void createContextualUnities(List<Unit> units) {
		unitiesInContext.clear();
		for (Unit u : units) {
			if (!unitiesInContext.containsKey(u.UIName)) {
				unitiesInContext.put(u.UIName, new Unity());
			}
			unitiesInContext.get(u.UIName).add(u);
		}
	}

	public static void selectUnityInContext(Unity unityID) {
		selectUnitInContext(unityID.UIName);
	}

	public static void selectUnitInContext(int unitID) {
		Unit target = getUnit(unitID);
		if (target != null) {
			selectUnitInContext(target.UIName);
		}
	}

	public static void selectUnitInContext(String id) {
		if (!multipleSelection) {
			unselect();
		}
		if(unitiesInContext.containsKey(id)) {
			for (Unit u : unitiesInContext.get(id)) {
				addToSelection(u);
			}
		}
	}

	public static ArrayList<Unity> getUnitiesInContext() {
		ArrayList<Unity> res = new ArrayList<>();
		for (Unity unity : unitiesInContext.values()) {
			res.add(unity);
		}
		return res;
	}

	public static CommandManager getInstance() {
		return instance;
	}

}
