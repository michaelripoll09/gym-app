package com.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.onboarding.*

private val Graphite = Color(0xFF0D0F10); private val Charcoal = Color(0xFF1C2022); private val Lime = Color(0xFFB9F227)
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { ProfileScreen() } } } }
@Composable private fun ProfileScreen() { var state by remember { mutableStateOf(ProfileSelectionState()) }; Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Paso 2 de 4", color=Lime); Text("¿Cómo entrenas?", color=Color.White, fontSize=32.sp); Text("Elige tu perfil principal", color=Color.LightGray); LazyVerticalGrid(GridCells.Fixed(2), Modifier.weight(1f), horizontalArrangement=Arrangement.spacedBy(12.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) { items(TrainingProfile.entries) { profile -> Card(onClick={ state=state.copy(primary=profile, validationMessage=null) }, colors=CardDefaults.cardColors(containerColor=if(state.primary==profile) Lime else Charcoal)) { Text(profile.label, Modifier.padding(18.dp), color=if(state.primary==profile) Graphite else Color.White) } } }; state.validationMessage?.let { Text(it,color=Color.Red) }; Button(onClick={}, enabled=state.primary!=null, colors=ButtonDefaults.buttonColors(containerColor=Lime, contentColor=Graphite), modifier=Modifier.fillMaxWidth()) { Text("Continuar") } } }
