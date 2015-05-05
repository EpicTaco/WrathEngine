#Note that parentObject is pre-defined in the Python environment and can be used to access Game.java
#This is NOT true if the game is launched via Python script, as the launch script must define the Game object.
from wrath.client.input import Key
from java.lang import Runnable

class HBind(Runnable):
	def run(self):
		parentObject.getGameLogger().log("You just pressed H!")

parentObject.getInputManager().bindKey(Key.KEY_H, HBind())
