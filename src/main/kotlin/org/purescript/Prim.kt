package org.purescript

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory

@Service(Service.Level.PROJECT)
class Prim(project: Project) {
    val file = PsiFileFactory.getInstance(project)
        .createFileFromText("Prim.purs", PSLanguage, """module Prim where
foreign import data Function :: Type -> Type -> Type
foreign import data Array :: Type -> Type
foreign import data Record :: Row Type -> Type
foreign import data Number :: Type
foreign import data Int :: Type
foreign import data String :: Type
foreign import data Char :: Type
foreign import data Boolean :: Type
foreign import data Type :: Type
foreign import data Constraint :: Type
foreign import data Symbol :: Type
foreign import data Row :: Type -> Type
""")
}