package com.apozas.contactdiary

/*
    This file is part of Contact Diary.
    Contact Diary is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    Contact Diary is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with Contact Diary. If not, see <http://www.gnu.org/licenses/>.
    Copyright 2020 by Alex Pozas-Kerstjens (apozas)
*/

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        supportFragmentManager.beginTransaction().replace(R.id.container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val preferences = preferenceManager.sharedPreferences
            val prefsedit = preferences.edit()

            val oldTime = preferences.getString("reminder_time", "21:00").toString()
            val reminderTime = findPreference<EditTextPreference>("reminder_time")
            val reminderToggle = findPreference<SwitchPreference>("reminder_toggle") as SwitchPreference

            reminderTime?.setOnPreferenceChangeListener { _, newValue ->
                var isTimeGood = true
                val newTime = newValue as String
                if (newTime.split(":").size == 2) {
                    val timeparts = newValue.split(":")
                    if ((timeparts[0].toInt() > 23) || (timeparts[1].toInt() > 59)) {
                        Toast.makeText(
                            context,
                            getString(R.string.incorrect_alarm_time),
                            Toast.LENGTH_LONG
                        ).show()
                        isTimeGood = false
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.incorrect_alarm_time),
                        Toast.LENGTH_LONG
                    ).show()
                    isTimeGood = false
                }
                if ((newValue.toString() != oldTime) && isTimeGood) {
                    prefsedit.putString("reminder_time", newValue)
                    prefsedit.apply()
                    Toast.makeText(context, getString(R.string.alarm_modified), Toast.LENGTH_SHORT).show()
                    updateNotificationPreferences(reminderToggle.isEnabled)
                    true
                } else {
                    prefsedit.putString("reminder_time", oldTime)
                    prefsedit.apply()
                    false }
            }
            reminderToggle.setOnPreferenceChangeListener { _, newValue ->
                updateNotificationPreferences(newValue as Boolean)
                true
            }
        }

        private fun updateNotificationPreferences(on: Boolean) {

            val receiver = ComponentName(
                requireActivity().applicationContext, NotificationReceiver::class.java
            )
            val pm = requireActivity().applicationContext.packageManager
            val notificationHandler = NotificationHandler()
            if (on) {
                notificationHandler.scheduleNotification(requireActivity().applicationContext)
                pm.setComponentEnabledSetting(
                    receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            } else {
                notificationHandler.disableNotification(requireActivity().applicationContext)
                pm.setComponentEnabledSetting(
                    receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }
}
