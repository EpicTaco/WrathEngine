#This is the example entry file for the engine. An extension to the wrath.client.Game class must be present.
#It should also be noted that a global variable named 'parentObject' must be assigned to said Game class for
#additional scripts to work. Variable 'scriptsManager' is pre-defined for you (Java Object PythonScriptManager).
#Consult documentation on that class for more information, or take a look at our Wiki on our Github page!
#Github Wiki Page: https://github.com/EpicTaco/WrathEngine/wiki/Scripting/

#  Wrath Engine Scripting
#  Copyright (C) 2015  Trent Spears
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
from wrath.client import Game
from wrath.client.events import GameEventHandler
from wrath.client.enums import RenderMode
from wrath.test.client import TempWorld
from java.io import File

gameObject = None
world = None

class EventHandler(GameEventHandler):
	def onGameOpen(self):
		gameObject.getInputManager().bindDefaultEngineKeys()
		parentObject.getInputManager().addDefaultKeyBinding(Key.MOUSE_BUTTON_1, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "setgrass")
		parentObject.getInputManager().addDefaultKeyBinding(Key.MOUSE_BUTTON_2, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN,"setstone")
		parentObject.getInputManager().addDefaultKeyBinding(Key.MOUSE_BUTTON_3, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN,"setair")
		
	def onGameClose(self):
		if world is not None:
			world.save()
	
	def onLoadJavaPlugin(self, object):
		pass
	
	def onTick(self):
		pass
		
	def onResolutionChange(self, oldWidth, oldHeight, newWidth, newHeight):
		gameObject.getWindowManager().setResolution(oldWidth, oldHeight)
		
	def onWindowOpen(self):
		gameObject.getInputManager().setCursorEnabled(True)

class CustomGame(Game):
	def __init__(self):
		Game.__init__(self, "Test Client", "INDEV", 30.0, RenderMode.Mode2D)
		self.getEventManager().addGameEventHandler(EventHandler())
		#Setting this object to global variable 'parentObject'
		scriptsManager.setGlobalVariable("parentObject", self)
		
	def render(self):
		if world is not None:
			world.drawWorld()
	
	def getWorld(self):
		if world is not None:
			return world
		else:
			return None



gameObject = CustomGame()

if File("etc/world.dat").exists():
	world = TempWorld.load(File("etc/world.dat"))
else:
	world = TempWorld(64, File("etc/world.dat"))
if world is None:
	parentObject.getLogger().log("TempWorld is not compatible with Python Scripts!")

scriptsManager.loadScriptsFromDirectory(File("etc/scripts/autoexec"), True, True)
gameObject.start()