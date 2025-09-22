# Java to Kotlin Conversion Guide

This guide provides best practices for converting Java source code to Kotlin, focusing on writing idiomatic Kotlin code rather than direct translation.

## Core Principles

### 1. Avoid Force Unwrapping (`!!`)
Never use the `!!` operator when converting from Java. Instead, handle nullability properly:

**❌ Bad (Direct Translation)**
```kotlin
val user = getUser()!!
val name = user.getName()!!
```

**✅ Good (Idiomatic Kotlin)**
```kotlin
val user = getUser() ?: return
val name = user.name ?: "Unknown"

// Or using safe calls
getUser()?.let { user ->
    val name = user.name ?: "Unknown"
    // Use name safely
}
```

### 2. Think Kotlin-First, Not Java Translation
Don't convert line-by-line. Instead, redesign the code to leverage Kotlin's strengths.

## Data Classes and POJOs

**Java POJO:**
```java
public class User {
    private String name;
    private int age;
    private String email;

    public User(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        // boilerplate equals implementation
    }

    @Override
    public int hashCode() {
        // boilerplate hashCode implementation
    }
}
```

**✅ Kotlin Data Class:**
```kotlin
data class User(
    var name: String,
    var age: Int,
    val email: String
) {
    // equals, hashCode, toString, copy automatically generated
    // No boilerplate getters/setters needed
}
```

## Null Safety Patterns

### Safe Calls and Elvis Operator
**Java:**
```java
public String getUserDisplayName(User user) {
    if (user != null && user.getName() != null) {
        return user.getName();
    }
    return "Guest";
}
```

**✅ Kotlin:**
```kotlin
fun getUserDisplayName(user: User?): String {
    return user?.name ?: "Guest"
}
```

### Let Function for Null Checks
**Java:**
```java
if (user != null) {
    String name = user.getName();
    String email = user.getEmail();
    sendEmail(name, email);
}
```

**✅ Kotlin:**
```kotlin
user?.let { user ->
    sendEmail(user.name, user.email)
}
```

## Collections and Functional Programming

### List Operations
**Java:**
```java
List<String> names = new ArrayList<>();
for (User user : users) {
    if (user.getAge() >= 18) {
        names.add(user.getName().toUpperCase());
    }
}
```

**✅ Kotlin:**
```kotlin
val names = users
    .filter { it.age >= 18 }
    .map { it.name.uppercase() }
```

### Safe Collection Access
**Java:**
```java
public String getFirstUserName(List<User> users) {
    if (users != null && !users.isEmpty()) {
        User firstUser = users.get(0);
        if (firstUser != null) {
            return firstUser.getName();
        }
    }
    return null;
}
```

**✅ Kotlin:**
```kotlin
fun getFirstUserName(users: List<User>?): String? {
    return users?.firstOrNull()?.name
}
```

## String Handling

### String Templates
**Java:**
```java
String message = "Hello " + user.getName() + ", you are " + user.getAge() + " years old";
```

**✅ Kotlin:**
```kotlin
val message = "Hello ${user.name}, you are ${user.age} years old"
```

### Multi-line Strings
**Java:**
```java
String json = "{\n" +
    "  \"name\": \"" + user.getName() + "\",\n" +
    "  \"age\": " + user.getAge() + "\n" +
    "}";
```

**✅ Kotlin:**
```kotlin
val json = """
    {
      "name": "${user.name}",
      "age": ${user.age}
    }
""".trimIndent()
```

## Function and Method Conversion

### Default Parameters
**Java:**
```java
public void createUser(String name) {
    createUser(name, 0, null);
}

public void createUser(String name, int age) {
    createUser(name, age, null);
}

public void createUser(String name, int age, String email) {
    // implementation
}
```

**✅ Kotlin:**
```kotlin
fun createUser(
    name: String,
    age: Int = 0,
    email: String? = null
) {
    // implementation
}
```

### Extension Functions
**Java:**
```java
public static boolean isEmailValid(String email) {
    return email != null && email.contains("@");
}

// Usage: StringUtils.isEmailValid(email)
```

**✅ Kotlin:**
```kotlin
fun String?.isValidEmail(): Boolean {
    return this != null && contains("@")
}

// Usage: email.isValidEmail()
```

## Exception Handling

### Try-Catch as Expression
**Java:**
```java
String result;
try {
    result = riskyOperation();
} catch (Exception e) {
    result = "default";
}
```

**✅ Kotlin:**
```kotlin
val result = try {
    riskyOperation()
} catch (e: Exception) {
    "default"
}
```

### Use runCatching for Exception Handling
**✅ Kotlin:**
```kotlin
val result = runCatching {
    riskyOperation()
}.getOrDefault("default")

// Or with more control
val result = runCatching {
    riskyOperation()
}.fold(
    onSuccess = { it },
    onFailure = { "default" }
)
```

## Android-Specific Patterns

### View Binding
**Java:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    TextView textView = findViewById(R.id.textView);
    Button button = findViewById(R.id.button);

    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textView.setText("Clicked");
        }
    });
}
```

**✅ Kotlin:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val textView = findViewById<TextView>(R.id.textView)
    val button = findViewById<Button>(R.id.button)

    button.setOnClickListener {
        textView.text = "Clicked"
    }
}
```

### Better with View Binding:
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            binding.textView.text = "Clicked"
        }
    }
}
```

## Interface and Abstract Class Conversion

### Functional Interfaces (SAM)
**Java:**
```java
interface OnClickListener {
    void onClick();
}

// Usage
setOnClickListener(new OnClickListener() {
    @Override
    public void onClick() {
        // handle click
    }
});
```

**✅ Kotlin:**
```kotlin
fun interface OnClickListener {
    fun onClick()
}

// Usage
setOnClickListener {
    // handle click
}
```

### Sealed Classes for Type Safety
**Java:**
```java
public abstract class Result {
    public static class Success extends Result {
        public final String data;
        public Success(String data) { this.data = data; }
    }

    public static class Error extends Result {
        public final String message;
        public Error(String message) { this.message = message; }
    }
}
```

**✅ Kotlin:**
```kotlin
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
}

// Usage with when expression
fun handleResult(result: Result) = when (result) {
    is Result.Success -> println("Data: ${result.data}")
    is Result.Error -> println("Error: ${result.message}")
}
```

## Property Delegation

### Lazy Initialization
**Java:**
```java
private ExpensiveObject expensiveObject;

public ExpensiveObject getExpensiveObject() {
    if (expensiveObject == null) {
        expensiveObject = new ExpensiveObject();
    }
    return expensiveObject;
}
```

**✅ Kotlin:**
```kotlin
private val expensiveObject by lazy {
    ExpensiveObject()
}
```

### Observable Properties
**Java:**
```java
private String name;

public void setName(String name) {
    String oldName = this.name;
    this.name = name;
    onNameChanged(oldName, name);
}
```

**✅ Kotlin:**
```kotlin
var name: String by Delegates.observable("") { _, oldValue, newValue ->
    onNameChanged(oldValue, newValue)
}
```

## Conversion Strategy

### 1. Analyze Before Converting
- Identify null-safety requirements
- Look for opportunities to use data classes
- Find places where functional programming can simplify code
- Consider if sealed classes would improve type safety

### 2. Convert in Logical Groups
- Convert data models first (POJOs → data classes)
- Convert utility functions (static methods → extension functions)
- Convert interfaces and abstract classes
- Convert main logic classes

### 3. Refactor After Basic Conversion
- Replace verbose null checks with safe calls
- Use `when` expressions instead of if-else chains
- Apply functional programming to collections
- Use property delegation where appropriate

### 4. Test Thoroughly
- Ensure null safety hasn't introduced new bugs
- Verify that functional transformations work correctly
- Test edge cases, especially around nullability

## Common Pitfalls to Avoid

1. **Don't use `!!` operator** - Always handle nullability properly
2. **Don't translate getters/setters directly** - Use properties instead
3. **Don't ignore Kotlin's null safety** - Embrace it for better code
4. **Don't convert everything at once** - Do it incrementally
5. **Don't forget about coroutines** - Consider async operations
6. **Don't ignore scope functions** - Use `let`, `apply`, `run`, `with`, `also` appropriately

## Tools and IDE Support

- Use IntelliJ IDEA/Android Studio's "Convert Java File to Kotlin"
- But always review and refactor the generated code
- Use Kotlin code style guidelines
- Enable Kotlin compiler warnings for best practices

## Summary

Converting Java to Kotlin is not just about syntax translation—it's about embracing Kotlin's philosophy of concise, safe, and expressive code. Focus on:

- **Null safety** without force unwrapping
- **Immutability** where possible
- **Functional programming** for collection operations
- **Data classes** for simple data holders
- **Extension functions** for utility methods
- **Sealed classes** for type-safe hierarchies

The goal is to write idiomatic Kotlin that's more maintainable, safer, and more expressive than the original Java code.