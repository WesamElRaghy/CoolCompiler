# Three-Address Code IR

# Class Base

# Attribute x : Int
x = 10

# Method getX : Int
method_getX:
return x

# Class Main
# Inherits from Base

# Attribute y : Int
y = 20

# Attribute z : Bool
z = true

# Method add : Int
method_add:
# Param n1 : Int
# Param n2 : Int
t0 = n1 + n2
return t0

# Method testIf : Int
method_testIf:
if z goto then_0
goto else_1
then_0:
t1 = y
goto endif_2
else_1:
t1 = x
endif_2:
return t1
