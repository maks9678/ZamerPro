package com.example.zamerpro.materials

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zamerpro.Materials
import com.example.zamerpro.room.TypeRoom

@Composable
fun MaterialsScreen(
    houseId:String,
    navController: NavController, ) {
}
@Preview(showBackground = true)
@Composable
fun PreviewMaterialsScreen(){

}
@Composable
fun MaterialsScreenIternal(roomName: String,
                           roomArea: String,
                           roomMetre: String,
                           onRoomNameChange: (String) -> Unit){
    Scaffold() { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = onRoomNameChange,
                    label = { Text("Название комнаты") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = roomName.isBlank()
                )
            }
            item {
                // Горизонтальная прокрутка для чипов
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(TypeRoom.entries.toTypedArray()) { roomType ->
                        SuggestionChip(
                            onClick = { onRoomTypeSelected(roomType) },
                            label = { Text(roomType.displayName) }
                        )
                    }
                }
            }
}
@Composable
fun MaterialItem(material: Materials){
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp){
        Text(text=material.name)
    })
}