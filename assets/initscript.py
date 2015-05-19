#This is the example entry file for the engine. An extension to the wrath.client.Game class must be present.
#The variable 'scriptsManager' is pre-defined for you (Java Object PythonScriptManager).
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
from java.io import File
from org.lwjgl.opengl import GL11
from wrath.client import Game
from wrath.client.events import GameEventHandler
from wrath.client.enums import RenderMode
from wrath.client.graphics import Model
from wrath.client.graphics import EntityRenderer
from wrath.client.graphics import ShaderProgram
from wrath.client.graphics import Texture

gameObject = None
entity = None

class CustomGame(Game, GameEventHandler):
	def __init__(self):
		Game.__init__(self, "Test Client", "INDEV", 60.0, RenderMode.Mode3D)
		self.getEventManager().addGameEventHandler(self)
		
	def onGameOpen(self):
		gameObject.getInputManager().bindKeysToEngineDefault()
		
	def onGameClose(self):
		pass
	
	def onLoadJavaPlugin(self, object):
		pass
	
	def onTick(self):
		entity.transformRotation(1.0, 1.0, 0.0)
		
	def onResolutionChange(self, oldWidth, oldHeight, newWidth, newHeight):
		pass
		
	def onWindowOpen(self):
		model = Model.createModel([-0.5,0.5,-0.5,-0.5,-0.5,-0.5,0.5,-0.5,-0.5,0.5,0.5,-0.5,-0.5,0.5,0.5,-0.5,-0.5,0.5,0.5,-0.5,0.5,0.5,0.5,0.5,0.5,0.5,-0.5,0.5,-0.5,-0.5,0.5,-0.5,0.5,0.5,0.5,0.5,-0.5,0.5,-0.5,-0.5,-0.5,-0.5,-0.5,-0.5,0.5,-0.5,0.5,0.5,-0.5,0.5,0.5,-0.5,0.5,-0.5,0.5,0.5,-0.5,0.5,0.5,0.5,-0.5,-0.5,0.5,-0.5,-0.5,-0.5,0.5,-0.5,-0.5,0.5,-0.5,0.5], [0,1,3,3,1,2,4,5,7,7,5,6,8,9,11,11,9,10,12,13,15,15,13,14,16,17,19,19,17,18,20,21,23,23,21,22], True)
		model.attachTexture(Texture(File("assets/textures/texture.png")), [0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0])
		entity.bindModel(model)
		entity.setScreenPosition(0.0,0.0,-5.0)
		gameObject.getInputManager().setCursorEnabled(True)

	def render(self):
		entity.render()

gameObject = CustomGame()
entity = EntityRenderer(None)

scriptsManager.loadScriptsFromDirectory(File("etc/scripts/autoexec"), True, True)
gameObject.start()