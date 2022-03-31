package spring.rce.hotfix

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ANEWARRAY
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.ARRAYLENGTH
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.GETFIELD
import org.objectweb.asm.Opcodes.IADD
import org.objectweb.asm.Opcodes.ICONST_0
import org.objectweb.asm.Opcodes.ICONST_1
import org.objectweb.asm.Opcodes.ICONST_2
import org.objectweb.asm.Opcodes.ICONST_3
import org.objectweb.asm.Opcodes.ICONST_4
import org.objectweb.asm.Opcodes.IFNONNULL
import org.objectweb.asm.Opcodes.INVOKESTATIC

class PatchDataBinderClassVisitor(cv: ClassVisitor) : ClassVisitor(ASM9, cv) {
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "getDisallowedFields" && mv != null) {
            return PatchDataBinderMethodVisitor(mv)
        }
        return mv
    }
}

class PatchDataBinderMethodVisitor(private val target: MethodVisitor) : MethodVisitor(ASM8) {
    private val orgSpringframeworkValidationDataBinderType = "org/springframework/validation/DataBinder"
    private val javaLangStringType = "java/lang/String"
    private val javaLangStringArrayDescriptor = "[L$javaLangStringType;"
    private val orgSpringframeworkBeansPropertyAccessorUtilsType = "org/springframework/beans/PropertyAccessorUtils"
    private val javaLangObjectType = "java/lang/Object"
    private val javaLangObjectDescriptor = "L$javaLangObjectType;"
    private val javaLangSystemType = "java/lang/System"
    private val constants = mapOf(
        ICONST_0 to "class.*",
        ICONST_1 to "Class.*",
        ICONST_2 to "*.class.*",
        ICONST_3 to "*.Class.*"
    )

    override fun visitCode() {
        target.visitCode();
        target.visitInsn(ICONST_4)
        target.visitTypeInsn(ANEWARRAY, javaLangStringType)

        constants.forEach { (opcode, value) ->
            target.visitInsn(DUP)
            target.visitInsn(opcode)
            target.visitLdcInsn(value)
            target.visitInsn(AASTORE)
        }

        target.visitMethodInsn(INVOKESTATIC, orgSpringframeworkBeansPropertyAccessorUtilsType, "canonicalPropertyNames", "($javaLangStringArrayDescriptor)$javaLangStringArrayDescriptor", false)

        target.visitVarInsn(ASTORE, 1)
        target.visitVarInsn(ALOAD, 0)
        target.visitFieldInsn(GETFIELD, orgSpringframeworkValidationDataBinderType, "disallowedFields", javaLangStringArrayDescriptor)
        val label37 = Label()
        target.visitJumpInsn(IFNONNULL, label37)
        target.visitVarInsn(ALOAD, 1)
        target.visitInsn(ARETURN)
        target.visitLabel(label37)
        target.visitVarInsn(ALOAD, 0)
        target.visitFieldInsn(GETFIELD, orgSpringframeworkValidationDataBinderType, "disallowedFields", javaLangStringArrayDescriptor)
        target.visitInsn(ARRAYLENGTH)
        target.visitVarInsn(ALOAD, 1)
        target.visitInsn(ARRAYLENGTH)
        target.visitInsn(IADD)
        target.visitTypeInsn(ANEWARRAY, javaLangStringType)
        target.visitVarInsn(ASTORE, 2)
        target.visitVarInsn(ALOAD, 0)
        target.visitFieldInsn(GETFIELD, orgSpringframeworkValidationDataBinderType, "disallowedFields", javaLangStringArrayDescriptor)
        target.visitInsn(ICONST_0)
        target.visitVarInsn(ALOAD, 2)
        target.visitInsn(ICONST_0)
        target.visitVarInsn(ALOAD, 0)
        target.visitFieldInsn(GETFIELD, orgSpringframeworkValidationDataBinderType, "disallowedFields", javaLangStringArrayDescriptor)
        target.visitInsn(ARRAYLENGTH)
        target.visitMethodInsn(INVOKESTATIC, javaLangSystemType, "arraycopy", "(${javaLangObjectDescriptor}I${javaLangObjectDescriptor}II)V", false)
        target.visitVarInsn(ALOAD, 1)
        target.visitInsn(ICONST_0)
        target.visitVarInsn(ALOAD, 2)
        target.visitVarInsn(ALOAD, 0)
        target.visitFieldInsn(GETFIELD, orgSpringframeworkValidationDataBinderType, "disallowedFields", javaLangStringArrayDescriptor)
        target.visitInsn(ARRAYLENGTH)
        target.visitVarInsn(ALOAD, 1)
        target.visitInsn(ARRAYLENGTH)
        target.visitMethodInsn(INVOKESTATIC, javaLangSystemType, "arraycopy", "(${javaLangObjectDescriptor}I${javaLangObjectDescriptor}II)V", false)
        target.visitVarInsn(ALOAD, 2)
        target.visitInsn(ARETURN)
        target.visitMaxs(5, 3)
        target.visitEnd()
    }
}
