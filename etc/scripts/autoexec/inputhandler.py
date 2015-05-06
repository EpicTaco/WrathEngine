#Note that parentObject is pre-defined in the Python environment and can be used to access Game.java
#This is NOT true if the game is launched via Python script, as the launch script must define the Game object.

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