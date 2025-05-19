.section .text
.global main

# # Three-Address Code IR
# # Class Base
# # Attribute x : Int
# # ELIMINATED UNUSED: x = 10
# # Method getX : Int
method_getX:
    push %rbp
    mov %rsp, %rbp
    sub $0, %rsp  # Stack space placeholder
    mov $10, %rax
# # Class Main
# # Inherits from Base
# # Attribute y : Int
# # ELIMINATED UNUSED: y = 20
# # Attribute z : Bool
# # ELIMINATED UNUSED: z = true
# # Method add : Int
    mov %rbp, %rsp
    pop %rbp
    ret

method_add:
    push %rbp
    mov %rsp, %rbp
    sub $0, %rsp  # Stack space placeholder
# # Param n1 : Int
# # Param n2 : Int
    # Load n1 into %rax
    mov $0, %rax  # Placeholder
    # Load n2 into %rbx
    mov $0, %rbx  # Placeholder
    add %rbx, %rax
    mov %rax, -8(%rbp)
    mov -8(%rbp), %rax
# # Method testIf : Int
    mov %rbp, %rsp
    pop %rbp
    ret

method_testIf:
    push %rbp
    mov %rsp, %rbp
    sub $0, %rsp  # Stack space placeholder
    mov $1, %rax
    cmp $0, %rax
    jne then_0
    jmp else_1
then_0:
# # ELIMINATED UNUSED: t1 = 20
    jmp endif_2
else_1:
    mov $10, -8(%rbp)
endif_2:
    mov $20, %rax
    mov %rbp, %rsp
    pop %rbp
    ret


# End of assembly code
