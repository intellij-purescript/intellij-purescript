package org.purescript.inference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getClassDeclarations
import org.purescript.getValueDeclarationGroupByName
import org.purescript.module.declaration.classes.PSClassMember

class InferenceIntegrationTest: BasePlatformTestCase() {
    fun `test everything`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f a = a
                | 
                | x = f 1
                | 
                | int :: Int -> Int
                | int x = x
                | 
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals(InferType.function(InferType.Id(0), InferType.Id(0)).toString(), "$f")
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals(InferType.function(InferType.Int, InferType.Int), int)
        val x = Main.getValueDeclarationGroupByName("x").inferType()
        TestCase.assertEquals(InferType.Int, x)
    }
    fun `test primitives`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | int = 42
                | number = 42.0
                | string = "Hello World"
                | boolean = true
            """.trimMargin()
        )
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        val number = Main.getValueDeclarationGroupByName("number").inferType()
        val string = Main.getValueDeclarationGroupByName("string").inferType()
        val boolean = Main.getValueDeclarationGroupByName("boolean").inferType()
        TestCase.assertEquals("Int", int.toString())
        TestCase.assertEquals("Number", number.toString())
        TestCase.assertEquals("String", string.toString())
        TestCase.assertEquals("Boolean", boolean.toString())
    }
    fun `test forall`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. a -> a
                | f a = a
                | 
                | int = f 10
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals("forall a. a -> a", pprint("$f"))
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int", "$int")
    }
    fun `test records`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | record =
                |  { int: 42
                |  , number: 42.0
                |  , string: "Hello World"
                |  , boolean: true
                |  }
                | int = record.int
                | 
                | type User = { age:: Int, name :: String }
                | 
                | mkUser :: User -> Int
                | mkUser user = 42
                | 
                | checkUser u = mkUser u
                | 
            """.trimMargin()
        )
        val record = Main.getValueDeclarationGroupByName("record").inferType()
        TestCase.assertEquals(
            "{ int::Int, number::Number, string::String, boolean::Boolean }",
            record.toString()
        )
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int", int.toString())

        val checkUserType = Main.getValueDeclarationGroupByName("checkUser").inferType()
        TestCase.assertEquals("{ age::Int, name::String } -> Int", "$checkUserType")

    }
    fun `test signature`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. a -> a
                | f x = x
                | 
                | class Eq a where
                |   eq :: a -> a -> Boolean
                | 
                | type_class :: forall a. Eq a => a -> a
                | type_class x = x
                | 
                | int = type_class 1
            """.trimMargin()
        )
        val type_class = Main.getValueDeclarationGroupByName("type_class").inferType()
        TestCase.assertEquals("forall a. Eq a => a -> a",  pprint(type_class.toString()))

        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int", int.toString())
    }
    
    fun `test union`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. Union (name :: String) (age :: Int) a => Record a -> Int
                | f x = 42
                | 
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals(
            "Row.Union (name::String) (age::Int) (name::String, age::Int) => { name::String, age::Int } -> Int",
            f.toString()
        )
    }
    
    fun `test type synonym`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | type Name = String
                | type Box a = a
                | 
                | name :: Name
                | name = "Haskell"
                | 
                | boxName :: Box String
                | boxName = "Box"
                | 
            """.trimMargin()
        )
        val name = Main.getValueDeclarationGroupByName("name").inferType()
        TestCase.assertEquals("String", "$name")
        val boxName = Main.getValueDeclarationGroupByName("boxName").inferType()
        TestCase.assertEquals("String", "$boxName")
    }
    fun `test new type`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | newtype Box a = Box a
                | 
                | boxString :: Box String
                | boxString = Box "Box" 
                | 
                | boxNoType = Box "Box"
                | 
            """.trimMargin()
        )
        val boxString = Main.getValueDeclarationGroupByName("boxString").inferType()
        TestCase.assertEquals("Box String", "$boxString")
        val boxNoType = Main.getValueDeclarationGroupByName("boxNoType").inferType()
        TestCase.assertEquals("Box String", "$boxNoType")
    }
    fun `test data type`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | data Box a = Full a | Empty
                | 
                | fullBox = Full "Box" 
                | 
                | emptyBox = Empty
                | 
            """.trimMargin()
        )
        val fullBox = Main.getValueDeclarationGroupByName("fullBox").inferType()
        TestCase.assertEquals("Box String", "$fullBox")
        val emptyBox = Main.getValueDeclarationGroupByName("emptyBox").inferType()
        TestCase.assertEquals("Box a", pprint("$emptyBox"))
    }
    fun `test classes`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | class Functor f where
                |   map :: forall a b. (a -> b) -> f a -> f b
                | 
                | class Foldable f where
                |   foldr :: forall a b. (a -> b -> b) -> b -> f a -> b
                |   foldl :: forall a b. (b -> a -> b) -> b -> f a -> b
                |   foldMap :: forall a m. Monoid m => (a -> m) -> f a -> m
                | 
                | intToInt :: Int -> Int
                | intToInt x = x
                | 
                | mapInt = map intToInt
                | 
            """.trimMargin()
        )
        val map = Main.getClassDeclarations()
            .first { it.name == "Functor"}
            .classMembers
            .single()
            .inferType()
        TestCase.assertEquals(
            "Functor a => forall b. forall c. (b -> c) -> a b -> a c",
            pprint("$map")
        )
        val mapInt = Main.getValueDeclarationGroupByName("mapInt").inferType()
        TestCase.assertEquals("a Int -> a Int",  pprint("$mapInt"))
        val foldable = Main.getClassDeclarations()
            .first { it.name == "Foldable" }
        val foldr = foldable
            .classMembers
            .also { it.map(PSClassMember::inferType) }
            .single { it.name == "foldr"}
            .inferType()
        TestCase.assertEquals(
            "Foldable a => forall b. forall c. (b -> c -> c) -> c -> a b -> c",
            pprint("$foldr")
        )
    }
    
    fun `test binders referencing data`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | data Box a = Box a
                | 
                | unbox (Box x) = x
                | 
            """.trimMargin()
        )
        val unbox =  Main.getValueDeclarationGroupByName("unbox").inferType()
        TestCase.assertEquals(
            "Box a -> a",
            pprint("$unbox")
        )
    }
    
    fun `test literal binder`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | 
                | int 1 = 1
                | int _ = 1
                | 
                | number 1.0 = 1.0
                | number _ = 1.0
                | 
                | bool true = true
                | bool _ = true
                | 
                | string "true" = "true"
                | string _ = "true"
                | 
            """.trimMargin()
        )
        
        val int =  Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int -> Int", "$int")
        
        val number =  Main.getValueDeclarationGroupByName("number").inferType()
        TestCase.assertEquals("Number -> Number", "$number")
        
        val bool =  Main.getValueDeclarationGroupByName("bool").inferType()
        TestCase.assertEquals("Boolean -> Boolean", "$bool")
        
        val string =  Main.getValueDeclarationGroupByName("string").inferType()
        TestCase.assertEquals("String -> String", "$string")
    }
    
    fun `test binders referencing newtype`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | newtype Box a = Box a
                | 
                | unbox (Box x) = x
                | 
            """.trimMargin()
        )
        val unbox =  Main.getValueDeclarationGroupByName("unbox").inferType()
        TestCase.assertEquals("Box a -> a", pprint("$unbox"))
    }
    
    fun pprint(string : String): String {
        val letters = ('a'..'z').joinToString("")
        val id = Regex("u\\d+")
        val us: Map<String, CharSequence> = id.findAll(string)
            .map { it.value }
            .sorted()
            .distinct()
            .withIndex()
            .associate { it.value to letters[it.index].toString() }
        return string.replace(id) { us[it.value]?: it.value}
    }
}