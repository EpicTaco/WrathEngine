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
from java.io import File
from wrath.client.graphics import Color

gameObject = None

class EventHandler(GameEventHandler):
	def onGameOpen(self):
		gameObject.getInputManager().bindDefaultEngineKeys()
		
	def onGameClose(self):
		pass
	
	def onLoadJavaPlugin(self, object):
		pass
	
	def onTick(self):
		pass
		
	def onResolutionChange(self, oldWidth, oldHeight, newWidth, newHeight):
		pass
		
	def onWindowOpen(self):
		gameObject.getInputManager().setCursorEnabled(True)

class CustomGame(Game):
	def __init__(self):
		Game.__init__(self, "Test Client", "INDEV", 30.0, RenderMode.Mode2D)
		self.getEventManager().addGameEventHandler(EventHandler())
		#Setting this object to global variable 'parentObject'
		scriptsManager.setGlobalVariable("parentObject", self)
		
	def render(self):
		self.getWindowManager().getFontRenderer().renderString("%d" % self.getWindowManager().getFPS(), -1.0, 0.94)
	
	def getWorld(self):
		pass

gameObject = CustomGame()

scriptsManager.loadScriptsFromDirectory(File("etc/scripts/autoexec"), True, True)
gameObject.start()