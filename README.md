### Echelon

*Echelon* is a simple programming language designed to be small in syntax, easy to implement and have no predefined functions.

### Syntax
There are the total of *4* single-character keywords in *echelon*:

- `!` defines a new function
- `~` returns from a function
- `:` runs a function
- `,` separates values

You can define a function using this syntax:
```
![name][:args]
 [instructions]
~[value]
```
As you see, any of the components except `!` and `~` (and newline between them) can be voided, for a total of `!\~` (`\` character serves the same purpose as a newline)


Identifiers, used to define function and variable names, are not bound to just letters. They can use anything that doesn't confuse it with regular *echelon* values/expressions.

For example, `name`, `for`, `$$$`, `$!(cool1name)!$` are valid identifiers, while `!hi`, `,a` `cool:name`, `1one1` are not.

#### Types
There are 4 literal value types in *echelon*: *Number*, *String*, *Function* and *Reference*

Strings act as usual, arbitrary text surrounded by `"`

Numbers represent double precision float values

Functions can be run using `:`, and are defined the same way as a regular functions, but without a name

References are sort of variables, that can have no value and be passed in functions.

Example of references:

```
!modify:arg
 set:arg,5
~

!
 set:var1,10
 modify:var1
 output:var1

 modify:var2
 output:var2
~
```

In this example, `set` and `output` are builtin functions, `modify` is a defined function that changes its argument's reference value. In both variants `output` outputs the value of *5*

This happens because function `modify` is modifying its argument directly. If you want to prevent it from happening, you can assign arguments value to a different reference:

```
!not-modify:arg
 set:var,arg
 set:var,9
~
```

That way value of passed reference is not changed, because function `not-modify` is modifying reference defined within itself.

#### Instructions

Instructions are nested function calls that drive the function. You can define as many instructions in a function as you want.

Instructions are executed from top to bottom (from right to left), and consist of 2 actions: Input a value, Run a function.

Look at this example: `x:a,y:b,c`. Here, `x` and `y` are functions, both taking 2 values and `y` returning 1 value. Considering `:` is used to run the function before it, let's deconstruct it into actions:

1. Input `c`
2. Input `b`
3. Run `y` (and input its return value)
4. Input `a`
5. Run `x`

Important to note, that when running the function, inputted values are flipped, so `y` function is ran with arguments `(b, c)`, and not `(c, b)`

If not enough values were inputted before running the function, error is thrown.

Another example, using 0 argument functions (procedures): `x:a,y::`, where `y` is a procedure that returns a function value:

1. Run `y` (and input its return value)
2. Run the latest inputted value
3. Input `a`
4. Run `x`
#### Builtins

There are **no** builtin functions or operators in *echelon*. Not even arithmetics.

You can define your own builtin functions using the [setBuiltinFunctions](src/main/java/me/white/echelon/environment/Environment.java#L41) or [addBuiltinFunction](src/main/java/me/white/echelon/environment/Environment.java#L45) in [Environment](src/main/java/me/white/echelon/environment/Environment.java#L12) class, with the name of your function and the instance of [Function.Builtin](src/main/java/me/white/echelon/Function.java#L69), which can be created with amount of arguments and your java function. Java function takes 2 parameters: [context](src/main/java/me/white/echelon/environment/Context.java) and the list of parameters (reference values), and should return resulting value or null.

With provided parameters you can get their stored values, modify them, and get the reference name. Latter one can be used in multitude of ways:

1. OOP like members: `class:method:a,b`
2. Syntax-abusing expressions: `math:(a+b)/10` (keep in mind that `(a+b)/10` is a valid identifier)

Note that when passing reference as an argument, it does not change its name to that of the argument. For example:

```
!output_math:expression
 output:math:expression
~

!
 output_math:(4+12)/2
~
```

here, `math` function will recieve a reference with name `(4+12)/2`, not `expression`.

Context can be used to get variables from calling function, get environment, its functions and global variables, and call other functions keeping the context of the calling functions, which can be used for conditions and loops.

Also you can create your own values, overriding [Value](src/main/java/me/white/echelon/Value.java) or [Function](src/main/java/me/white/echelon/Function.java) classes.
