#Note that parentObject is pre-defined in the Python environment and can be used to access Game.java
#This is NOT true if the game is launched via Python script, as the launch script must define the Game object.
from wrath.client.input import Key
from wrath.client.input import KeyAction
from java.lang import Runnable
from wrath.test.client import TempWorld

class SetGrass(Runnable):
	def run(self):
		tile = parentObject.getWorld().getBounds(parentObject.getInputManager().getCursorX(), parentObject.getInputManager().getCursorY())
		if len(tile) >= 2:
			parentObject.getWorld().setTile(tile[0], tile[1], TempWorld.GRASS)

class SetStone(Runnable):
	def run(self):
		tile = parentObject.getWorld().getBounds(parentObject.getInputManager().getCursorX(), parentObject.getInputManager().getCursorY())
		if len(tile) >= 2:
			parentObject.getWorld().setTile(tile[0], tile[1], TempWorld.STONE)

class SetAir(Runnable):
	def run(self):
		tile = parentObject.getWorld().getBounds(parentObject.getInputManager().getCursorX(), parentObject.getInputManager().getCursorY())
		if len(tile) >= 2:
			parentObject.getWorld().setTile(tile[0], tile[1], TempWorld.AIR)

parentObject.getInputManager().addSavedFunction("setgrass", SetGrass())
parentObject.getInputManager().addSavedFunction("setstone", SetStone())
parentObject.getInputManager().addSavedFunction("setair", SetAir())