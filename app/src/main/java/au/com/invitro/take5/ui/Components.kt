package au.com.invitro.take5.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import au.com.invitro.take5.R
import au.com.invitro.take5.data.Risk
import au.com.invitro.take5.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IvtTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    largeBranding: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (largeBranding) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ivt_logo),
                    contentDescription = "In Vitro Technologies",
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .aspectRatio(216f / 65f),
                )
                Text(
                    title,
                    color = IvtTeal,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(Modifier.fillMaxWidth().height(3.dp).background(IvtTeal))
        }
        return
    }

    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ivt_logo),
                        contentDescription = "In Vitro Technologies",
                        modifier = Modifier.height(30.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        title,
                        color = IvtTeal,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = IvtTeal)
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        )
        Box(Modifier.fillMaxWidth().height(3.dp).background(IvtTeal))
    }
}

@Composable
fun StepCard(
    step: String,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().background(IvtTeal).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(step, color = IvtTeal, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false,
    multiline: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (required) "$label *" else label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(capitalization = capitalization),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (required) "$label *" else label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
fun CheckRow(text: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).clickable(onClick = onToggle).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RiskChip(level: Risk.Level?, modifier: Modifier = Modifier) {
    Surface(
        color = level?.let { Color(it.argb) } ?: Color(0xFFB8C4C9),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            level?.label?.uppercase() ?: "NOT RATED",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun RiskSelector(
    title: String,
    likelihood: Int,
    consequence: Int,
    onLikelihood: (Int) -> Unit,
    onConsequence: (Int) -> Unit,
) {
    val lOptions = Risk.likelihoods.mapIndexed { i, s -> "${i + 1} – $s" }
    val cOptions = Risk.consequences.mapIndexed { i, s -> "${i + 1} – $s" }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        RiskChip(Risk.level(likelihood, consequence))
    }
    DropdownField(
        label = "Likelihood",
        options = lOptions,
        selected = lOptions.getOrElse(likelihood - 1) { "" },
        onSelect = { onLikelihood(lOptions.indexOf(it) + 1) },
        required = true,
    )
    DropdownField(
        label = "Consequence",
        options = cOptions,
        selected = cOptions.getOrElse(consequence - 1) { "" },
        onSelect = { onConsequence(cOptions.indexOf(it) + 1) },
        required = true,
    )
}

@Composable
fun FileImage(file: File, modifier: Modifier = Modifier, maxPx: Int = 400, contentScale: ContentScale = ContentScale.Crop) {
    val bitmap by produceState<ImageBitmap?>(null, file.path) {
        value = withContext(Dispatchers.IO) { ImageUtils.decodeSampled(file, maxPx)?.let(Bitmap::asImageBitmap) }
    }
    val bmp = bitmap
    if (bmp != null) {
        Image(bmp, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier.background(Color(0xFFE1E1E1)))
    }
}

@Composable
fun SignaturePad(
    strokes: SnapshotStateList<List<Offset>>,
    onSizeChanged: (IntSize) -> Unit,
    strokeWidthPx: Float,
    modifier: Modifier = Modifier,
) {
    val current = remember { mutableStateListOf<Offset>() }
    Canvas(
        modifier
            .border(1.dp, Color(0xFFB8C4C9), RoundedCornerShape(4.dp))
            .background(Color.White)
            .onSizeChanged(onSizeChanged)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    current.clear()
                    current.add(down.position)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        current.add(change.position)
                        change.consume()
                    }
                    strokes.add(current.toList())
                    current.clear()
                }
            },
    ) {
        val style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        (strokes + listOf(current.toList())).filter { it.isNotEmpty() }.forEach { stroke ->
            if (stroke.size == 1) {
                drawCircle(Color.Black, radius = strokeWidthPx / 2, center = stroke[0])
            } else {
                val path = Path().apply {
                    moveTo(stroke[0].x, stroke[0].y)
                    stroke.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, Color.Black, style = style)
            }
        }
    }
}

@Composable
fun dpToPx(dp: Float): Float = with(LocalDensity.current) { dp.dp.toPx() }
