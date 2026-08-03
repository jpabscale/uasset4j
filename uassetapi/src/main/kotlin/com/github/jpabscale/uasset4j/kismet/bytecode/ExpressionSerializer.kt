// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/ExpressionSerializer.cs
package com.github.jpabscale.uasset4j.kismet.bytecode

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_AddMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ArrayConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ArrayGetByRef
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Assert
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_BindDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_BitFieldConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Breakpoint
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ByteConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CallMath
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CallMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClassContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClassSparseDataVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClearMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ComputedJump
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Context
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Context_FailSilent
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CrossInterfaceCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DefaultVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DeprecatedOp4A
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DoubleConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DynamicCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndArray
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndArrayConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndFunctionParms
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndMap
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndMapConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndOfScript
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndParmValue
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndSet
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndSetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndStructConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_False
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_FieldPathConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_FinalFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_FloatConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InstanceDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InstanceVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InstrumentationEvent
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Int64Const
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntConstByte
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InterfaceContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InterfaceToObjCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntOne
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntZero
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Jump
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_JumpIfNot
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Let
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetBool
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetObj
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetValueOnPersistentFrame
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetWeakObjPtr
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalFinalFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalOutVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalVirtualFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_MapConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_MetaCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NameConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NoInterface
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NoObject
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Nothing
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NothingInt32
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ObjectConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ObjToInterfaceCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PopExecutionFlow
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PopExecutionFlowIfNot
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PrimitiveCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PropertyConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PushExecutionFlow
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_RemoveMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Return
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_RotationConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Self
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetArray
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetMap
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetSet
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Skip
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SkipOffsetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SoftObjectConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StringConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StructConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StructMemberContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SwitchValue
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_TextConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Tracepoint
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_TransformConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_True
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_UInt64Const
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_UnicodeStringConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Vector3fConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VectorConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VirtualFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_WireTracepoint

object ExpressionSerializer {
    fun ReadExpression(reader: AssetBinaryReader): KismetExpression {
        val token = reader.ReadByte()
        val res: KismetExpression = when (token) {
            0x00 -> EX_LocalVariable()
            0x01 -> EX_InstanceVariable()
            0x02 -> EX_DefaultVariable()
            0x04 -> EX_Return()
            0x06 -> EX_Jump()
            0x07 -> EX_JumpIfNot()
            0x09 -> EX_Assert()
            0x0B -> EX_Nothing()
            0x0C -> EX_NothingInt32()
            0x0F -> EX_Let()
            0x11 -> EX_BitFieldConst()
            0x12 -> EX_ClassContext()
            0x13 -> EX_MetaCast()
            0x14 -> EX_LetBool()
            0x15 -> EX_EndParmValue()
            0x16 -> EX_EndFunctionParms()
            0x17 -> EX_Self()
            0x18 -> EX_Skip()
            0x19 -> EX_Context()
            0x1A -> EX_Context_FailSilent()
            0x1B -> EX_VirtualFunction()
            0x1C -> EX_FinalFunction()
            0x1D -> EX_IntConst()
            0x1E -> EX_FloatConst()
            0x37 -> EX_DoubleConst()
            0x1F -> EX_StringConst()
            0x20 -> EX_ObjectConst()
            0x21 -> EX_NameConst()
            0x22 -> EX_RotationConst()
            0x23 -> EX_VectorConst()
            0x24 -> EX_ByteConst()
            0x25 -> EX_IntZero()
            0x26 -> EX_IntOne()
            0x27 -> EX_True()
            0x28 -> EX_False()
            0x29 -> EX_TextConst()
            0x2A -> EX_NoObject()
            0x2B -> EX_TransformConst()
            0x2C -> EX_IntConstByte()
            0x2D -> EX_NoInterface()
            0x2E -> EX_DynamicCast()
            0x2F -> EX_StructConst()
            0x30 -> EX_EndStructConst()
            0x31 -> EX_SetArray()
            0x32 -> EX_EndArray()
            0x33 -> EX_PropertyConst()
            0x34 -> EX_UnicodeStringConst()
            0x35 -> EX_Int64Const()
            0x36 -> EX_UInt64Const()
            0x38 -> EX_PrimitiveCast()
            0x39 -> EX_SetSet()
            0x3A -> EX_EndSet()
            0x3B -> EX_SetMap()
            0x3C -> EX_EndMap()
            0x3D -> EX_SetConst()
            0x3E -> EX_EndSetConst()
            0x3F -> EX_MapConst()
            0x40 -> EX_EndMapConst()
            0x41 -> EX_Vector3fConst()
            0x42 -> EX_StructMemberContext()
            0x43 -> EX_LetMulticastDelegate()
            0x44 -> EX_LetDelegate()
            0x45 -> EX_LocalVirtualFunction()
            0x46 -> EX_LocalFinalFunction()
            0x48 -> EX_LocalOutVariable()
            0x4A -> EX_DeprecatedOp4A()
            0x4B -> EX_InstanceDelegate()
            0x4C -> EX_PushExecutionFlow()
            0x4D -> EX_PopExecutionFlow()
            0x4E -> EX_ComputedJump()
            0x4F -> EX_PopExecutionFlowIfNot()
            0x50 -> EX_Breakpoint()
            0x51 -> EX_InterfaceContext()
            0x52 -> EX_ObjToInterfaceCast()
            0x53 -> EX_EndOfScript()
            0x54 -> EX_CrossInterfaceCast()
            0x55 -> EX_InterfaceToObjCast()
            0x5A -> EX_WireTracepoint()
            0x5B -> EX_SkipOffsetConst()
            0x5C -> EX_AddMulticastDelegate()
            0x5D -> EX_ClearMulticastDelegate()
            0x5E -> EX_Tracepoint()
            0x5F -> EX_LetObj()
            0x60 -> EX_LetWeakObjPtr()
            0x61 -> EX_BindDelegate()
            0x62 -> EX_RemoveMulticastDelegate()
            0x63 -> EX_CallMulticastDelegate()
            0x64 -> EX_LetValueOnPersistentFrame()
            0x65 -> EX_ArrayConst()
            0x66 -> EX_EndArrayConst()
            0x67 -> EX_SoftObjectConst()
            0x68 -> EX_CallMath()
            0x69 -> EX_SwitchValue()
            0x6A -> EX_InstrumentationEvent()
            0x6B -> EX_ArrayGetByRef()
            0x6C -> EX_ClassSparseDataVariable()
            0x6D -> EX_FieldPathConst()
            else -> throw NotImplementedError("Unimplemented token $token")
        }
        res.Read(reader)
        return res
    }

    fun WriteExpression(expr: KismetExpression, writer: AssetBinaryWriter): Int {
        writer.WriteByte(expr.Token.value)
        return expr.Write(writer) + 1
    }
}
