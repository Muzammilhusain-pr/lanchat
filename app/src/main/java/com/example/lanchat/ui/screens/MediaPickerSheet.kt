package com.example.lanchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class AttachmentOption { CAMERA, GALLERY, VIDEO, DOCUMENT, LOCATION, CONTACT }

/**
 * The "+" attach sheet -- same set of options WhatsApp offers: camera,
 * gallery, video, document, location, contact. Wiring for each option:
 *
 *  - CAMERA/GALLERY/VIDEO/DOCUMENT -> Android's ActivityResultContracts
 *    (PickVisualMedia / OpenDocument) to get a Uri, then FileTransferManager.upload().
 *  - LOCATION -> LocationSharer.getCurrentLocationMessage().
 *  - CONTACT -> ActivityResultContracts.PickContact, then read name+number
 *    via ContentResolver and send as WireMessage(type = CONTACT).
 */
@Composable
fun MediaPickerSheet(onDismiss: () -> Unit, onOptionSelected: (AttachmentOption) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            AttachRow(Icons.Filled.CameraAlt, "Camera") { onOptionSelected(AttachmentOption.CAMERA) }
            AttachRow(Icons.Filled.Image, "Gallery") { onOptionSelected(AttachmentOption.GALLERY) }
            AttachRow(Icons.Filled.Videocam, "Video") { onOptionSelected(AttachmentOption.VIDEO) }
            AttachRow(Icons.Filled.Description, "Document") { onOptionSelected(AttachmentOption.DOCUMENT) }
            AttachRow(Icons.Filled.LocationOn, "Location") { onOptionSelected(AttachmentOption.LOCATION) }
            AttachRow(Icons.Filled.Person, "Contact") { onOptionSelected(AttachmentOption.CONTACT) }
        }
    }
}

@Composable
private fun AttachRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = label) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
