local cad = component.proxy(component.list("cad")())
local cmp = component.proxy(component.list("computer")())
local epr = component.proxy(component.list("eeprom")())
local caster = cad.selectorCaster()
local pos = cad.operatorEntityPosition(caster)
local look = cad.operatorEntityLook(caster)
local vconst = cad.operatorVectorConstruct -- Methods are quite lenghty, you can shorten them
local vsum = cad.operatorVectorSum
cad.trickDebug(pos)
cad.trickDebug(look)
cad.trickDebug("Hello from lua")
cad.print("Hello again") -- Prints in the console
cad.trickAddMotion(caster,look,1)
cad.trickConjureBlock(vsum(vsum(pos,look),vconst(0,1,0)),40)
epr.setLabel("Testing") -- You can modify the eeprom and it will keep the modifications
cmp.stop() -- to avoid halt beeps