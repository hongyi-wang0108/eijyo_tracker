package com.eijyo.tracker.data.local

import androidx.room.TypeConverter
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.ConfidenceLevel
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.DocumentCategory
import com.eijyo.tracker.data.model.DocumentStatus
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.RequiredLevel
import com.eijyo.tracker.data.model.ResultType
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.model.SupplementStatus
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.VisaType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters. Enums persist as their stable [Enum.name]; Room itself
 * handles null columns, so non-null converters also serve nullable fields.
 * String lists persist as a JSON array.
 */
class Converters {

    @TypeConverter fun toAppStatus(v: String): ApplicationStatus = enumValueOf(v)
    @TypeConverter fun fromAppStatus(v: ApplicationStatus): String = v.name

    @TypeConverter fun toResultType(v: String): ResultType = enumValueOf(v)
    @TypeConverter fun fromResultType(v: ResultType): String = v.name

    @TypeConverter fun toTriState(v: String): TriState = enumValueOf(v)
    @TypeConverter fun fromTriState(v: TriState): String = v.name

    @TypeConverter fun toVisaType(v: String): VisaType = enumValueOf(v)
    @TypeConverter fun fromVisaType(v: VisaType): String = v.name

    @TypeConverter fun toAppPath(v: String): ApplicationPath = enumValueOf(v)
    @TypeConverter fun fromAppPath(v: ApplicationPath): String = v.name

    @TypeConverter fun toOffice(v: String): ImmigrationOffice = enumValueOf(v)
    @TypeConverter fun fromOffice(v: ImmigrationOffice): String = v.name

    @TypeConverter fun toIncomeRange(v: String): IncomeRange = enumValueOf(v)
    @TypeConverter fun fromIncomeRange(v: IncomeRange): String = v.name

    @TypeConverter fun toDatePrecision(v: String): DatePrecision = enumValueOf(v)
    @TypeConverter fun fromDatePrecision(v: DatePrecision): String = v.name

    @TypeConverter fun toDocCategory(v: String): DocumentCategory = enumValueOf(v)
    @TypeConverter fun fromDocCategory(v: DocumentCategory): String = v.name

    @TypeConverter fun toRequiredLevel(v: String): RequiredLevel = enumValueOf(v)
    @TypeConverter fun fromRequiredLevel(v: RequiredLevel): String = v.name

    @TypeConverter fun toDocStatus(v: String): DocumentStatus = enumValueOf(v)
    @TypeConverter fun fromDocStatus(v: DocumentStatus): String = v.name

    @TypeConverter fun toSupplementStatus(v: String): SupplementStatus = enumValueOf(v)
    @TypeConverter fun fromSupplementStatus(v: SupplementStatus): String = v.name

    @TypeConverter fun toRiskLevel(v: String): RiskLevel = enumValueOf(v)
    @TypeConverter fun fromRiskLevel(v: RiskLevel): String = v.name

    @TypeConverter fun toConfidence(v: String): ConfidenceLevel = enumValueOf(v)
    @TypeConverter fun fromConfidence(v: ConfidenceLevel): String = v.name

    @TypeConverter fun toStringList(v: String): List<String> = Json.decodeFromString<List<String>>(v)
    @TypeConverter fun fromStringList(v: List<String>): String = Json.encodeToString(v)
}
