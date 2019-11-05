[![](https://jitpack.io/v/PsHegger/recycleradapter-generator.svg)](https://jitpack.io/#PsHegger/recycleradapter-generator)

## What is RecyclerAdapter Generator (RAG)?
Many Android Developers have to write the same boilerplate code for displaying a simple RecyclerView.
This library tries to be a solution to minimize the written code which is required to write adapters for simple `RecyclerView`s.

## Setup
RAG is currently only available on [Jitpack](https://jitpack.io/).

### Gradle
Check if `jitpack` is added as a repository.

```gradle
// Project level build.gradle
allprojects {
    repositories {
        // your other repositories
        maven { url 'https://jitpack.io' }
    }
}
```

### Dependencies
RAG consist of two separate artifacts: `annotations` which will be used to mark your classes for processing, and `codegen` which will generate the final code for you.

```gradle
implementation "com.github.pshegger.recycleradapter-generator:annotations:0.1.0"

kapt "com.github.pshegger.recycleradapter-generator:codegen:0.1.0"
```

Add these 2 lines to your module dependencies and you can start using RAG.

## Quick Start
Use `@ModelBinding` to annotate a class to be used for adapter generation.

```kotlin
@ModelBinding(
    R.layout.foo,           // layout id
    clickListener = false,  // if true an item click listener is generated for your adapter, default: false 
    namePrefix = ""         // default: empty -> generate name from the name of the data class
)
class UserBinding(private val user: User) {
    // binding methods
}
```

To create a method which binds the values of your data class to a `View` create a method annotated with `@BindView` inside your class.

```kotlin
@BindView(R.id.name)
fun setName(nameLabel: TextView) {
    nameLabel.text = user.name
}
```

If everything is ready press build and after it finishes your adapter will be generated (in the above example it will be called `UserAdapter`).
From this point you can use at as it was written by you.

## Features

- [x] Basic adapter generation
- [x] Item click listener support
- [x] Custom adapter name (only prefix)
- [ ] Update Dispatching (using DiffUtil)
- [ ] Click listener for specific Views
- [ ] Artifacts for Maven Central

Feel free to suggest new features.
