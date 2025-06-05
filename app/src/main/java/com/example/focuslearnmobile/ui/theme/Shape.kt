// app/src/main/java/com/example/focuslearnmobile/ui/theme/Shape.kt
package com.example.focuslearnmobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val FocusLearnShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimensions.RadiusSm),
    small = RoundedCornerShape(Dimensions.RadiusLg),
    medium = RoundedCornerShape(Dimensions.RadiusXl),
    large = RoundedCornerShape(Dimensions.Radius2Xl),
    extraLarge = RoundedCornerShape(Dimensions.Radius3Xl)
)