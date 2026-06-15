package com.example.matlabcalculator

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.PI

enum class CalculatorMode(val displayName: String) {
    STANDARD("Standard"),
    SCIENTIFIC("Scientific"),
    GRAPHING("Graphing"),
    PROGRAMMER("Programmer"),
    DATE_CALCULATION("Date Calculation")
}

data class HistoryEntry(
    val expression: String,
    val result: String,
    val isError: Boolean = false
)

data class GraphEquation(
    val formula: String,
    val color: Color,
    val isVisible: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF202020), // Windows Dark Theme
                    surface = Color(0xFF1F1F1F),
                    primary = Color(0xFF005A9E),    // Windows Accent Blue
                    onBackground = Color(0xFFFFFFFF),
                    onSurface = Color(0xFFFFFFFF)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationWrapper()
                }
            }
        }
    }
}

@Composable
fun AppNavigationWrapper() {
    var currentMode by remember { mutableStateOf(CalculatorMode.SCIENTIFIC) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF202020),
                modifier = Modifier.fillMaxHeight().width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Calculator Modes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                HorizontalDivider(color = Color(0xFF30363D))
                Spacer(modifier = Modifier.height(8.dp))

                CalculatorMode.values().forEach { mode ->
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = mode.displayName,
                                fontSize = 16.sp,
                                fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentMode == mode) Color(0xFF58A6FF) else Color.White
                            ) 
                        },
                        selected = currentMode == mode,
                        onClick = {
                            currentMode = mode
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF2C2C2C),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        val onMenuClick: () -> Unit = { scope.launch { drawerState.open() } }

        when (currentMode) {
            CalculatorMode.STANDARD -> StandardCalculatorScreen(onMenuClick)
            CalculatorMode.SCIENTIFIC -> ScientificCalculatorScreen(onMenuClick)
            CalculatorMode.GRAPHING -> GraphingCalculatorScreen(onMenuClick)
            CalculatorMode.PROGRAMMER -> ProgrammerCalculatorScreen(onMenuClick)
            CalculatorMode.DATE_CALCULATION -> DateCalculationScreen(onMenuClick)
        }
    }
}

// -------------------------------------------------------------
// 1. STANDARD CALCULATOR SCREEN
// -------------------------------------------------------------
@Composable
fun StandardCalculatorScreen(onMenuClick: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var displayResult by remember { mutableStateOf("0") }

    fun evaluateStandard() {
        val raw = expression.trim()
        if (raw.isEmpty()) return
        try {
            val res = Evaluator().evaluate(raw)
            displayResult = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
        } catch (e: Exception) {
            displayResult = "Error"
        }
    }

    fun handlePress(key: String) {
        when (key) {
            "C" -> {
                expression = ""
                displayResult = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) expression = expression.dropLast(1)
            }
            "=" -> evaluateStandard()
            "÷" -> expression += "/"
            "×" -> expression += "*"
            "−" -> expression += "-"
            "+" -> expression += "+"
            "±" -> {
                if (expression.startsWith("-")) expression = expression.substring(1)
                else expression = "-$expression"
            }
            else -> expression += key
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Standard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(expression.ifEmpty { " " }, fontSize = 20.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(displayResult, fontSize = 64.sp, color = Color.White)
        }

        val keys = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )

        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1F1F1F)).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (key in row) {
                        val isNum = key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
                        val isOp = key in listOf("÷", "×", "−", "+", "=")
                        val bgColor = when {
                            key == "=" -> MaterialTheme.colorScheme.primary
                            isNum -> Color(0xFF3B3B3B)
                            isOp -> Color(0xFF323232)
                            else -> Color(0xFF2D2D2D)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f).fillMaxHeight().background(bgColor).clickable { handlePress(key) }
                        ) {
                            Text(key, color = Color.White, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. SCIENTIFIC CALCULATOR SCREEN
// -------------------------------------------------------------
@Composable
fun ScientificCalculatorScreen(onMenuClick: () -> Unit) {
    var expression: String by remember { mutableStateOf("") }
    var displayResult: String by remember { mutableStateOf("0") }
    var isRadMode: Boolean by remember { mutableStateOf(true) }
    var history: List<HistoryEntry> by remember { mutableStateOf(emptyList()) }
    var isHistoryOpen: Boolean by remember { mutableStateOf(false) }

    var trigExpanded by remember { mutableStateOf(false) }
    var funcExpanded by remember { mutableStateOf(false) }

    fun evaluateExpression() {
        val rawInput = expression.trim()
        if (rawInput.isEmpty()) return

        try {
            val evaluator = Evaluator()
            val parsedResult = evaluator.evaluate(rawInput)

            val formattedResult = if (parsedResult % 1.0 == 0.0) {
                parsedResult.toLong().toString()
            } else {
                parsedResult.toString()
            }

            displayResult = formattedResult
            history = listOf(HistoryEntry(rawInput, formattedResult)) + history
        } catch (e: Exception) {
            displayResult = "Error"
            history = listOf(HistoryEntry(rawInput, e.message ?: "Invalid Syntax", isError = true)) + history
        }
    }

    fun handleKeyPress(key: String) {
        when (key) {
            "C" -> {
                expression = ""
                displayResult = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) expression = expression.dropLast(1)
            }
            "=" -> evaluateExpression()
            "x²" -> expression += "^2"
            "xʸ" -> expression += "^"
            "1/x" -> expression += "1/("
            "|x|" -> expression += "abs("
            "√x" -> expression += "sqrt("
            "10ˣ" -> expression += "10^"
            "mod" -> expression += "mod"
            "n!" -> expression += "!"
            "±" -> {
                if (expression.startsWith("-")) expression = expression.substring(1)
                else expression = "-$expression"
            }
            "π" -> expression += "pi"
            "e" -> expression += "e"
            "÷" -> expression += "/"
            "×" -> expression += "*"
            "−" -> expression += "-"
            "+" -> expression += "+"
            else -> expression += key
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scientific", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Native Material History Icon
                IconButton(onClick = { isHistoryOpen = !isHistoryOpen }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = if (isHistoryOpen) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(expression.ifEmpty { " " }, fontSize = 18.sp, color = Color(0xFFA0A0A0), textAlign = TextAlign.End, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(displayResult, fontSize = 54.sp, fontWeight = FontWeight.Light, color = Color.White, textAlign = TextAlign.End, maxLines = 1)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isRadMode = !isRadMode }) {
                    Text(if (isRadMode) "RAD" else "DEG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("F-E", color = Color(0xFF808080), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("MC", "MR", "M+", "M-", "MS").forEach { memKey ->
                    Text(memKey, fontSize = 13.sp, color = Color(0xFF808080), modifier = Modifier.clickable {}.padding(4.dp))
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2C2C2C))
                            .clickable { trigExpanded = !trigExpanded; if (trigExpanded) funcExpanded = false }
                            .padding(symmetricTrigPadding(trigExpanded)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📐 Trigonometry ${if (trigExpanded) "▴" else "▾"}", color = Color.White, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2C2C2C))
                            .clickable { funcExpanded = !funcExpanded; if (funcExpanded) trigExpanded = false }
                            .padding(symmetricTrigPadding(funcExpanded)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ƒ Function ${if (funcExpanded) "▴" else "▾"}", color = Color.White, fontSize = 14.sp)
                    }
                }

                AnimatedVisibility(visible = trigExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    TrigSubKeypad(onKeyPress = { handleKeyPress(it); trigExpanded = false })
                }

                AnimatedVisibility(visible = funcExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    FuncSubKeypad(onKeyPress = { handleKeyPress(it); funcExpanded = false })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1F1F1F)).padding(4.dp)) {
                ScientificGrid(onKeyPress = { handleKeyPress(it) })
            }
        }

        AnimatedVisibility(visible = isHistoryOpen, enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(), exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.75f).align(Alignment.CenterEnd).background(Color(0xFF202020)).padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("History", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { history = emptyList() }) { Text("Clear", color = Color(0xFFFF7B72)) }
                    }
                    HorizontalDivider(color = Color(0xFF30363D), modifier = Modifier.padding(vertical = 8.dp))

                    if (history.isEmpty()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("There's no history yet", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(history) { entry ->
                                Column(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        expression = entry.expression
                                        displayResult = entry.result
                                        isHistoryOpen = false
                                    }.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(entry.expression, color = Color.Gray, fontSize = 14.sp)
                                    Text(entry.result, color = if (entry.isError) Color(0xFFFF7B72) else Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. GRAPHING CALCULATOR SCREEN (Equation vs Plot tab support)
// -------------------------------------------------------------
@Composable
fun GraphingCalculatorScreen(onMenuClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Equations, 1 = Graph Plot
    
    // Support multiple equations plotting with custom colors and visibility toggles
    var equations by remember {
        mutableStateOf(
            listOf(
                GraphEquation("sin(x)", Color(0xFF58A6FF), true),
                GraphEquation("x^2 - 4", Color(0xFFFF7B72), false),
                GraphEquation("cos(x)", Color(0xFF56D364), false)
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Graphing", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Segmented Tabs to switch between Equation Mode and Graphing Mode
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1F1F1F),
            contentColor = Color(0xFF58A6FF)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Equations", color = Color.White, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Graph Plot", color = Color.White, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // --- EQUATION MODE SCREEN ---
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(equations.size) { index ->
                    val eq = equations[index]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Color dot indicator
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(eq.color)
                            )
                            
                            // Formula text field
                            TextField(
                                value = eq.formula,
                                onValueChange = { newFormula ->
                                    equations = equations.mapIndexed { idx, item ->
                                        if (idx == index) item.copy(formula = newFormula) else item
                                    }
                                },
                                label = { Text("y${index + 1}(x)", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            // Visibility switch
                            Switch(
                                checked = eq.isVisible,
                                onCheckedChange = { isChecked ->
                                    equations = equations.mapIndexed { idx, item ->
                                        if (idx == index) item.copy(isVisible = isChecked) else item
                                    }
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // --- GRAPHING PLOT MODE SCREEN ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161B22))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val xMin = -10.0
                    val xMax = 10.0
                    val yMin = -10.0
                    val yMax = 10.0

                    fun toPixelX(x: Double): Float = ((x - xMin) / (xMax - xMin) * width).toFloat()
                    fun toPixelY(y: Double): Float = ((1.0 - (y - yMin) / (yMax - yMin)) * height).toFloat()

                    // Draw coordinate axis lines
                    drawLine(Color(0xFF8B949E), start = androidx.compose.ui.geometry.Offset(0f, toPixelY(0.0)), end = androidx.compose.ui.geometry.Offset(width, toPixelY(0.0)), strokeWidth = 2f)
                    drawLine(Color(0xFF8B949E), start = androidx.compose.ui.geometry.Offset(toPixelX(0.0), 0f), end = androidx.compose.ui.geometry.Offset(toPixelX(0.0), height), strokeWidth = 2f)

                    // Draw thin grid lines
                    for (i in -8..8 step 2) {
                        if (i == 0) continue
                        val gridX = toPixelX(i.toDouble())
                        val gridY = toPixelY(i.toDouble())
                        drawLine(Color(0xFF30363D), start = androidx.compose.ui.geometry.Offset(gridX, 0f), end = androidx.compose.ui.geometry.Offset(gridX, height), strokeWidth = 1f)
                        drawLine(Color(0xFF30363D), start = androidx.compose.ui.geometry.Offset(0f, gridY), end = androidx.compose.ui.geometry.Offset(width, gridY), strokeWidth = 1f)
                    }

                    // Plot all active visible equations
                    equations.forEach { eq ->
                        if (eq.isVisible && eq.formula.isNotEmpty()) {
                            val path = Path()
                            var first = true
                            val step = 0.05
                            var x = xMin
                            val evaluator = Evaluator()

                            while (x <= xMax) {
                                try {
                                    val y = evaluator.evaluate(eq.formula.replace("x", "($x)"))
                                    if (!y.isNaN() && !y.isInfinite()) {
                                        val px = toPixelX(x)
                                        val py = toPixelY(y)

                                        if (py >= 0f && py <= height) {
                                            if (first) {
                                                path.moveTo(px, py)
                                                first = false
                                            } else {
                                                path.lineTo(px, py)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore points that don't evaluate
                                }
                                x += step
                            }

                            if (!first) {
                                drawPath(
                                    path = path,
                                    color = eq.color,
                                    style = Stroke(width = 4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. PROGRAMMER CALCULATOR SCREEN (Fully robust base features)
// -------------------------------------------------------------
@Composable
fun ProgrammerCalculatorScreen(onMenuClick: () -> Unit) {
    var currentNumber by remember { mutableStateOf(0L) }
    var storedValue by remember { mutableStateOf(0L) }
    var activeOp by remember { mutableStateOf("") }
    var activeBase by remember { mutableStateOf("DEC") } // HEX, DEC, OCT, BIN

    fun isKeyEnabled(key: String, base: String): Boolean {
        if (key in listOf("A", "B", "C", "D", "E", "F")) return base == "HEX"
        if (key in listOf("8", "9")) return base == "HEX" || base == "DEC"
        if (key in listOf("2", "3", "4", "5", "6", "7")) return base in listOf("HEX", "DEC", "OCT")
        return true
    }

    fun handlePress(key: String) {
        when (key) {
            "C" -> {
                currentNumber = 0L
                storedValue = 0L
                activeOp = ""
            }
            "⌫" -> {
                currentNumber = when (activeBase) {
                    "HEX" -> currentNumber / 16
                    "OCT" -> currentNumber / 8
                    "BIN" -> currentNumber / 2
                    else -> currentNumber / 10
                }
            }
            "+", "−", "×", "÷", "AND", "OR", "XOR" -> {
                storedValue = currentNumber
                activeOp = key
                currentNumber = 0L
            }
            "=" -> {
                currentNumber = when (activeOp) {
                    "+" -> storedValue + currentNumber
                    "−" -> storedValue - currentNumber
                    "×" -> storedValue * currentNumber
                    "÷" -> if (currentNumber != 0L) storedValue / currentNumber else 0L
                    "AND" -> storedValue and currentNumber
                    "OR" -> storedValue or currentNumber
                    "XOR" -> storedValue xor currentNumber
                    else -> currentNumber
                }
                activeOp = ""
            }
            else -> {
                // Append digit
                val digitVal = if (key in listOf("A", "B", "C", "D", "E", "F")) {
                    key.toLong(16)
                } else {
                    key.toLong()
                }

                currentNumber = when (activeBase) {
                    "HEX" -> currentNumber * 16 + digitVal
                    "OCT" -> currentNumber * 8 + digitVal
                    "BIN" -> currentNumber * 2 + digitVal
                    else -> currentNumber * 10 + digitVal
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Programmer", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Active Calculation Box showing different Bases values
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFF161B22)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("HEX", "DEC", "OCT", "BIN").forEach { base ->
                val baseValueStr = when (base) {
                    "HEX" -> currentNumber.toString(16).uppercase()
                    "OCT" -> currentNumber.toString(8)
                    "BIN" -> currentNumber.toString(2)
                    else -> currentNumber.toString()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (activeBase == base) Color(0xFF2C2C2C) else Color.Transparent)
                        .clickable { activeBase = base }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(base, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(baseValueStr, color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Programmer Keypad Grid (includes Bitwise operations and A-F Hexadecimal keys)
        val keys = listOf(
            listOf("A", "B", "C", "D", "E", "F"),
            listOf("AND", "OR", "XOR", "C", "⌫", "÷"),
            listOf("7", "8", "9", "×", "", ""),
            listOf("4", "5", "6", "−", "", ""),
            listOf("1", "2", "3", "+", "", ""),
            listOf("0", "=", "", "", "", "")
        )

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF1F1F1F)).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (key in row) {
                        if (key.isEmpty()) {
                            Box(modifier = Modifier.weight(1f))
                            continue
                        }

                        val isEnabled = isKeyEnabled(key, activeBase)
                        val isNum = key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
                        val isOp = key in listOf("÷", "×", "−", "+", "=", "AND", "OR", "XOR")
                        
                        val bgColor = when {
                            key == "=" -> MaterialTheme.colorScheme.primary
                            isNum -> Color(0xFF3B3B3B)
                            isOp -> Color(0xFF323232)
                            else -> Color(0xFF2D2D2D)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(bgColor.copy(alpha = if (isEnabled) 1f else 0.2f))
                                .clickable(enabled = isEnabled) { handlePress(key) }
                        ) {
                            Text(
                                text = key,
                                color = Color.White.copy(alpha = if (isEnabled) 1f else 0.3f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. DATE CALCULATION SCREEN (Robust difference stats)
// -------------------------------------------------------------
@Composable
fun DateCalculationScreen(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }

    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.time = if (isStart) startDate else endDate
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, day)
                if (isStart) startDate = newCalendar.time
                else endDate = newCalendar.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val totalDays = run {
        val diff = abs(endDate.time - startDate.time)
        diff / (1000 * 60 * 60 * 24)
    }

    val years = totalDays / 365
    val months = (totalDays % 365) / 30
    val days = (totalDays % 365) % 30
    val totalWeeks = totalDays / 7

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Date Calculation", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("From Date", color = Color.Gray, fontSize = 14.sp)
                    Button(
                        onClick = { showDatePicker(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C))
                    ) {
                        Text(formatter.format(startDate), color = Color.White)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("To Date", color = Color.Gray, fontSize = 14.sp)
                    Button(
                        onClick = { showDatePicker(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C))
                    ) {
                        Text(formatter.format(endDate), color = Color.White)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Difference Details", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                    
                    Text(
                        text = "$years Years, $months Months, $days Days",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Weeks", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("$totalWeeks weeks", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Days", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("$totalDays days", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER COMPOSE SUB-KEYPADS AND GRIDS
// -------------------------------------------------------------
private fun symmetricTrigPadding(expanded: Boolean) = PaddingValues(
    horizontal = 12.dp,
    vertical = if (expanded) 8.dp else 12.dp
)

@Composable
fun TrigSubKeypad(onKeyPress: (String) -> Unit) {
    val trigKeys = listOf("sin", "cos", "tan", "asin", "acos", "atan")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C)).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(trigKeys) { key ->
            Button(
                onClick = { onKeyPress(key) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3B3B)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = key, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun FuncSubKeypad(onKeyPress: (String) -> Unit) {
    val funcKeys = listOf("abs", "exp", "log", "ln")
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C)).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(funcKeys) { key ->
            Button(
                onClick = { onKeyPress(key) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3B3B)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = key, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ScientificGrid(onKeyPress: (String) -> Unit) {
    val keys = listOf(
        listOf("2nd", "π", "e", "C", "⌫"),
        listOf("x²", "1/x", "|x|", "exp", "mod"),
        listOf("√x", "(", ")", "n!", "÷"),
        listOf("xʸ", "7", "8", "9", "×"),
        listOf("10ˣ", "4", "5", "6", "−"),
        listOf("log", "1", "2", "3", "+"),
        listOf("ln", "±", "0", ".", "=")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in keys) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (key in row) {
                    val isNumber = key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
                    val isOperator = key in listOf("÷", "×", "−", "+", "=")
                    
                    val bgColor = when {
                        key == "=" -> MaterialTheme.colorScheme.primary  // Accent color
                        isNumber -> Color(0xFF3B3B3B)                    // Lighter gray for numbers
                        isOperator -> Color(0xFF323232)                  // Dark gray for basic math operators
                        else -> Color(0xFF2D2D2D)                        // Charcoal gray for scientific modifiers
                    }

                    val textColor = when {
                        key == "=" -> Color.White
                        isOperator -> Color.White
                        else -> Color(0xFFE5E5E5)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(bgColor)
                            .clickable { onKeyPress(key) }
                    ) {
                        Text(
                            text = key,
                            color = textColor,
                            fontSize = if (isNumber) 22.sp else 16.sp,
                            fontWeight = if (isNumber || isOperator) FontWeight.Normal else FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}
