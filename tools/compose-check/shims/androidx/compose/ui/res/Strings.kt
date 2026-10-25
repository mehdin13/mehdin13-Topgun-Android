package androidx.compose.ui.res

import androidx.compose.runtime.Composable

@Composable
fun stringResource(id: Int): String = ""

@Composable
fun stringResource(id: Int, vararg formatArgs: Any): String = ""

@Composable
fun pluralStringResource(id: Int, count: Int): String = ""

@Composable
fun pluralStringResource(id: Int, count: Int, vararg formatArgs: Any): String = ""
