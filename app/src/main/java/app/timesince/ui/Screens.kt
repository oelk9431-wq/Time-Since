@file:OptIn(ExperimentalMaterial3Api::class)
package app.timesince.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.timesince.*
import app.timesince.data.*
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.*

private val palette = listOf(0xFF2E7D32, 0xFFC62828, 0xFF1565C0, 0xFFEF6C00, 0xFF6A1B9A, 0xFF00838F)

@Composable fun ticker(): Long { var now by remember { mutableLongStateOf(System.currentTimeMillis()) }; LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000 - now % 1000) } }; return now }

@Composable fun HomeScreen(vm: MainViewModel, detail: (Long)->Unit, settings: ()->Unit) {
    val counters by vm.counters.collectAsStateWithLifecycle(); val now = ticker(); var editor by remember { mutableStateOf<Counter?>(null) }; var adding by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title={ Text("Time Since", fontWeight=FontWeight.Bold) }, actions={ IconButton(settings){ Icon(Icons.Default.Settings,"Settings") }; IconButton({ adding=true }){ Icon(Icons.Default.Add,"Add counter") } }) }, floatingActionButton={ FloatingActionButton({adding=true}) { Icon(Icons.Default.Add,"Add counter") } }) { pad ->
        if(counters.isEmpty()) Box(Modifier.fillMaxSize().padding(pad), contentAlignment=Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally) { Icon(Icons.Default.Timer, null, Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No counters yet", style=MaterialTheme.typography.titleLarge); Button({adding=true}, Modifier.padding(12.dp)){Text("Add counter")} } }
        else LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding=PaddingValues(bottom=88.dp)) { items(counters, key={it.id}) { c ->
            ListItem(headlineContent={ Text(c.name, fontSize=19.sp) }, leadingContent={ TimerBadge(c, now) }, trailingContent={ Row { IconButton({vm.run{vm.repo.move(c,-1)}}){Icon(Icons.Default.KeyboardArrowUp,"Move up")}; IconButton({vm.run{vm.repo.move(c,1)}}){Icon(Icons.Default.KeyboardArrowDown,"Move down")} } }, modifier=Modifier.clickable{detail(c.id)}); HorizontalDivider()
        } }
    }
    if(adding) CounterEditor(null, vm.settings.value, {adding=false}) { vm.save(it,true); adding=false }
}

@Composable private fun TimerBadge(c: Counter, now: Long, large:Boolean=false) {
    val reached = c.type==CounterType.COUNTDOWN && c.isRunning && now>=c.eventAt
    Surface(color=Color(c.color), shape=RoundedCornerShape(12.dp)) { Text(if(reached) "Target reached" else TimerMath.format(TimerMath.valueMillis(c,now),c.format,c.showSeconds), color=readable(Color(c.color)), fontWeight=FontWeight.Bold, fontSize=if(large) 25.sp else 16.sp, modifier=Modifier.padding(horizontal=if(large) 24.dp else 12.dp, vertical=if(large) 18.dp else 10.dp)) }
}
private fun readable(c: Color)=if((c.red*.299+c.green*.587+c.blue*.114)>.6) Color.Black else Color.White

@Composable fun DetailScreen(vm:MainViewModel,id:Long,back:()->Unit,history:(Long)->Unit){
    val counter by vm.counter(id).collectAsStateWithLifecycle(null); val c=counter ?: return; val now=ticker(); val context=LocalContext.current
    var confirm by remember{mutableStateOf<String?>(null)}; var editor by remember{mutableStateOf(false)}; var formats by remember{mutableStateOf(false)}; var notificationSettings by remember{mutableStateOf(false)}
    val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
    Scaffold(topBar={TopAppBar(title={Text(c.name)},navigationIcon={IconButton(back){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}},actions={IconButton({ val text="${c.name} — ${TimerMath.format(TimerMath.valueMillis(c,now),DisplayFormat.LONG,false)}"; context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,text),"Share counter"))}){Icon(Icons.Default.Share,"Share")}})}){pad->
        LazyColumn(Modifier.fillMaxSize().padding(pad),horizontalAlignment=Alignment.CenterHorizontally,contentPadding=PaddingValues(20.dp)){
            item{Spacer(Modifier.height(12.dp));TimerBadge(c,now,true);Text(c.name,style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(14.dp));Section("ACTIONS");ActionGrid(listOf(Triple("Restart",Icons.Default.RestartAlt,{confirm="restart"}),Triple("From date",Icons.Default.CalendarMonth,{editor=true}),Triple(if(c.isRunning)"Stop" else "Start / Resume",if(c.isRunning)Icons.Default.Stop else Icons.Default.PlayArrow,{vm.setRunning(c,!c.isRunning)})));Section("OPTIONS");ActionGrid(listOf(Triple("Edit",Icons.Default.Edit,{editor=true}),Triple("Format",Icons.Default.Tune,{formats=true}),Triple("Delete",Icons.Default.Delete,{confirm="delete"})));Section("COMPLEMENTS");ActionGrid(listOf(Triple("Restarts",Icons.Default.History,{history(id)}),Triple("Notifications",Icons.Default.Notifications,{ notificationSettings=true })))}
            item{Text(if(c.notificationsEnabled)"Notifications enabled: ${c.notificationDays} day(s)" else "Notifications off",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(28.dp));val label=if(c.type==CounterType.SINCE)"Started" else "Target";Text("$label on ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(c.eventAt))}",style=MaterialTheme.typography.bodyMedium)}
        }
    }
    if(confirm!=null) AlertDialog(onDismissRequest={confirm=null},title={Text(if(confirm=="delete")"Delete this counter?" else "Restart this counter?")},text={Text(if(confirm=="delete")"This cannot be undone." else "The current period will be saved in restart history.")},confirmButton={TextButton({if(confirm=="delete")vm.run{vm.repo.delete(c);back()} else vm.restart(c);confirm=null}){Text(if(confirm=="delete")"Delete" else "Restart")}},dismissButton={TextButton({confirm=null}){Text("Cancel")}})
    if(editor)CounterEditor(c,vm.settings.value,{editor=false}){vm.save(it);editor=false}
    if(formats)FormatDialog(c,{formats=false}){vm.save(it);formats=false}
    if(notificationSettings) NotificationDialog(c,{notificationSettings=false}) { enabled, days ->
        if(enabled && Build.VERSION.SDK_INT>=33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        vm.save(c.copy(notificationsEnabled=enabled,notificationDays=days)); notificationSettings=false
    }
}

@Composable private fun Section(text:String){Text(text,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary,modifier=Modifier.fillMaxWidth().padding(top=22.dp,bottom=8.dp))}
@Composable private fun ActionGrid(actions:List<Triple<String,androidx.compose.ui.graphics.vector.ImageVector,()->Unit>>){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){actions.forEach{(name,icon,click)->FilledTonalButton(click,Modifier.weight(1f).height(72.dp),contentPadding=PaddingValues(4.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null);Text(name)}}}}}

@Composable private fun CounterEditor(existing:Counter?,defaults:AppSettings,dismiss:()->Unit,save:(Counter)->Unit){
    var name by remember{mutableStateOf(existing?.name.orEmpty())};var type by remember{mutableStateOf(existing?.type?:CounterType.SINCE)};var date by remember{mutableLongStateOf(existing?.eventAt?:System.currentTimeMillis())};var color by remember{mutableLongStateOf(existing?.color?:defaults.defaultColor)};val context=LocalContext.current
    AlertDialog(onDismissRequest=dismiss,title={Text(if(existing==null)"Add counter" else "Edit counter")},text={Column{OutlinedTextField(name,{name=it},label={Text("Counter name")},singleLine=true);Row(verticalAlignment=Alignment.CenterVertically){RadioButton(type==CounterType.SINCE,{type=CounterType.SINCE});Text("Time Since");RadioButton(type==CounterType.COUNTDOWN,{type=CounterType.COUNTDOWN});Text("Countdown")};OutlinedButton({pickDateTime(context,date){date=it}}){Icon(Icons.Default.CalendarMonth,null);Spacer(Modifier.width(8.dp));Text(DateFormat.getDateTimeInstance().format(Date(date)))};Text("Color",Modifier.padding(top=12.dp));Row{palette.forEach{p->Surface(color=Color(p),shape=RoundedCornerShape(50),modifier=Modifier.padding(4.dp).size(if(color==p)40.dp else 32.dp).clickable{color=p}){}}}}},confirmButton={Button({if(name.isNotBlank())save((existing?:Counter(name=name.trim(),type=type,eventAt=date)).copy(name=name.trim(),type=type,eventAt=date,color=color))},enabled=name.isNotBlank()){Text("Save")}},dismissButton={TextButton(dismiss){Text("Cancel")}})
}
private fun pickDateTime(context:android.content.Context,initial:Long,onPicked:(Long)->Unit){val cal=Calendar.getInstance().apply{timeInMillis=initial};DatePickerDialog(context,{_,y,m,d->TimePickerDialog(context,{_,h,min->cal.set(y,m,d,h,min,0);cal.set(Calendar.MILLISECOND,0);onPicked(cal.timeInMillis)},cal[Calendar.HOUR_OF_DAY],cal[Calendar.MINUTE],android.text.format.DateFormat.is24HourFormat(context)).show()},cal[Calendar.YEAR],cal[Calendar.MONTH],cal[Calendar.DAY_OF_MONTH]).show()}

@Composable private fun FormatDialog(c:Counter,dismiss:()->Unit,save:(Counter)->Unit){var f by remember{mutableStateOf(c.format)};var seconds by remember{mutableStateOf(c.showSeconds)};AlertDialog(onDismissRequest=dismiss,title={Text("Timer format")},text={LazyColumn{items(DisplayFormat.entries){item->Row(Modifier.fillMaxWidth().clickable{f=item},verticalAlignment=Alignment.CenterVertically){RadioButton(f==item,{f=item});Text(TimerMath.format(8_964_000,item,seconds))}};item{Row(verticalAlignment=Alignment.CenterVertically){Switch(seconds,{seconds=it});Text(" Show seconds")}}}},confirmButton={Button({save(c.copy(format=f,showSeconds=seconds))}){Text("Save")}},dismissButton={TextButton(dismiss){Text("Cancel")}})}

@Composable private fun NotificationDialog(c:Counter,dismiss:()->Unit,save:(Boolean,Int)->Unit){var enabled by remember{mutableStateOf(c.notificationsEnabled)};var days by remember{mutableIntStateOf(c.notificationDays)};AlertDialog(onDismissRequest=dismiss,title={Text("Notifications")},text={Column{Row(verticalAlignment=Alignment.CenterVertically){Switch(enabled,{enabled=it});Text(" Enable reminders")};Text(if(c.type==CounterType.SINCE)"Notify at milestone" else "Remind me before target",Modifier.padding(top=12.dp));listOf(1,7,30,100,365).forEach{value->Row(Modifier.fillMaxWidth().clickable{days=value},verticalAlignment=Alignment.CenterVertically){RadioButton(days==value,{days=value});Text("$value day${if(value==1)"" else "s"}")}}}},confirmButton={Button({save(enabled,days)}){Text("Save")}},dismissButton={TextButton(dismiss){Text("Cancel")}})}

@Composable fun HistoryScreen(vm:MainViewModel,id:Long,back:()->Unit){val history by vm.history(id).collectAsStateWithLifecycle(emptyList());var clear by remember{mutableStateOf(false)};Scaffold(topBar={TopAppBar(title={Text("Restart history")},navigationIcon={IconButton(back){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}},actions={if(history.isNotEmpty())IconButton({clear=true}){Icon(Icons.Default.DeleteSweep,"Clear history")}})}){pad->if(history.isEmpty())Box(Modifier.fillMaxSize().padding(pad),contentAlignment=Alignment.Center){Text("No restarts yet")}else LazyColumn(Modifier.padding(pad)){items(history){h->ListItem(headlineContent={Text(DateFormat.getDateTimeInstance().format(Date(h.restartedAt)))},supportingContent={Text("Previous start: ${DateFormat.getDateTimeInstance().format(Date(h.previousEventAt))}\nElapsed: ${TimerMath.format(h.elapsedMillis,DisplayFormat.LONG,false)}")});HorizontalDivider()}}};if(clear)AlertDialog(onDismissRequest={clear=false},title={Text("Clear restart history?")},confirmButton={TextButton({vm.run{vm.repo.clearHistory(id)};clear=false}){Text("Clear")}},dismissButton={TextButton({clear=false}){Text("Cancel")}})}

@Composable fun SettingsScreen(vm:MainViewModel,back:()->Unit){val settings by vm.settings.collectAsStateWithLifecycle();Scaffold(topBar={TopAppBar(title={Text("Settings")},navigationIcon={IconButton(back){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}})}){pad->Column(Modifier.padding(pad).padding(20.dp)){Text("Theme",style=MaterialTheme.typography.titleMedium);ThemeMode.entries.forEach{mode->Row(Modifier.fillMaxWidth().clickable{vm.setTheme(mode)},verticalAlignment=Alignment.CenterVertically){RadioButton(settings.theme==mode,{vm.setTheme(mode)});Text(mode.name.lowercase().replaceFirstChar{it.uppercase()})}};HorizontalDivider(Modifier.padding(vertical=16.dp));Text("Default timer format",style=MaterialTheme.typography.titleMedium);var expanded by remember{mutableStateOf(false)};Box{OutlinedButton({expanded=true}){Text(settings.defaultFormat.name)};DropdownMenu(expanded,{expanded=false}){DisplayFormat.entries.forEach{f->DropdownMenuItem({Text(f.name)},{vm.setDefaults(f,settings.defaultColor);expanded=false})}}};Text("Default color",Modifier.padding(top=16.dp));Row{palette.forEach{p->Surface(color=Color(p),shape=RoundedCornerShape(50),modifier=Modifier.padding(4.dp).size(if(settings.defaultColor==p)40.dp else 32.dp).clickable{vm.setDefaults(settings.defaultFormat,p)}){}}};HorizontalDivider(Modifier.padding(vertical=22.dp));Text("About",style=MaterialTheme.typography.titleMedium);Text("Time Since\nVersion ${BuildConfig.VERSION_NAME}\nPrivate, offline, and ad-free.",Modifier.padding(top=8.dp))}}
