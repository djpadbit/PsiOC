local cad = component.proxy(component.list("cad")())
local cmp = component.proxy(component.list("computer")())
local caster = cad.selectorCaster()
local pos = cad.operatorEntityPosition(caster)
local look = cad.operatorEntityLook(caster)
local time = 80
-- Requires Random PSIdeas
cad.rpsideas_trick_conjure_star(cad.operatorVectorMultiply(look,20),time,pos)
for i=0,10 do
	local rpos = cad.operatorVectorSum(pos,cad.operatorVectorMultiply(look,i/5))
	cad.rpsideas_trick_conjure_circle(rpos,look,time,1-(i/10))
end
cmp.stop() -- to avoid halt beeps