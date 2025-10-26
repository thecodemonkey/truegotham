package il.tutorials.truegotham.model.extensions

import il.tutorials.truegotham.utils.ByteUtils

fun ByteArray.toDataUrl(mimeType: String) = ByteUtils.toDataUrl(this, mimeType)
