# Three-Address Code IR

# Class Base

# Attribute x : Int
# ELIMINATED UNUSED: x = 10

# Method getX : Int
method_getX:
return 10

# Class Main
# Inherits from Base

# Attribute y : Int
# ELIMINATED UNUSED: y = 20

# Attribute z : Bool
# ELIMINATED UNUSED: z = true

# Method add : Int
method_add:
# Param n1 : Int
# Param n2 : Int
t0 = n1 + n2
return t0

# Method testIf : Int
method_testIf:
if true goto then_0
goto else_1
then_0:
# ELIMINATED UNUSED: t1 = 20
goto endif_2
else_1:
20 = 10
endif_2:
return 20
