#This is the example entry file for the engine. An extension to the wrath.client.Game class must be present.
#The variable 'scriptsManager' is pre-defined for you (Java Object PythonScriptManager).
#The variable 'launchargs' is also pre-defined if the engine was launched in Python (such as this script). Use this variable in the start() method.
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
from org.lwjgl.util.vector import Vector3f
from wrath.common.entities import GenericEntity
from wrath.client.graphics import EntityRenderer
from wrath.common.entities import EntityDescriptor
from wrath.client.input import Key
from wrath.client.input import KeyAction

class CustomGame(Game, GameEventHandler):
	def __init__(self):
            Game.__init__(self, "Test Client", "INDEV", 60.0, RenderMode.Mode3D)
            self.getEventManager().addGameEventHandler(self)
		
	def onGameOpen(self):
            self.getInputManager().setEngineKeysToDefault()
            self.getInputManager().bindKey(Key.KEY_UP, Key.MOD_SHIFT, KeyAction.KEY_HOLD_DOWN, "move_up")
            self.getInputManager().bindKey(Key.KEY_DOWN, Key.MOD_SHIFT, KeyAction.KEY_HOLD_DOWN, "move_down")
            global entity
            entity = EntityRenderer(GenericEntity(Vector3f(0.0,-1.5,-2.5), None, EntityDescriptor("body.obj", "white_texture.png", None, 0.5, 1.0, 0.0)))
	    renders.append(entity)

	def onGameClose(self):
            pass
	
	def onLoadJavaPlugin(self, loadedObject):
            pass
	
	def onTick(self):
	    for obj in renders:
		obj.getEntity().translateOrientation(0.0, 1.0, 0.0)
		
	def onResolutionChange(self, oldWidth, oldHeight, newWidth, newHeight):
            pass
		
	def onWindowOpen(self):
            pass

	def render(self):
            for obj in renders:
	    	obj.render()

scriptsManager.setGlobalVariable("renders", [])
CustomGame().start(launchargs)