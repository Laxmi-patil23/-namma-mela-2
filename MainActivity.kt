package com.nammamela.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("namma_mela_store", MODE_PRIVATE) }
    private val managerEmail = "2004laxmipatil@gmail.com"

    private var currentLanguage = "en"
    private var customerPhone = ""
    private var otp = ""
    private var pendingImageTarget = ""
    private var posterUri = ""
    private var actorUri = ""

    private val maroon = Color.rgb(153, 47, 37)
    private val leaf = Color.rgb(43, 111, 83)
    private val saffron = Color.rgb(224, 144, 45)
    private val ink = Color.rgb(37, 32, 26)
    private val muted = Color.rgb(105, 92, 77)
    private val paper = Color.rgb(255, 250, 242)
    private val panel = Color.WHITE
    private val border = Color.rgb(226, 214, 195)
    private val blue = Color.rgb(59, 96, 135)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        customerPhone = prefs.getString(KEY_CUSTOMER_PHONE, "").orEmpty()
        seedDemoData()
        showRoleSelection()
    }

    private fun showRoleSelection() {
        val body = screen(t("app_name"), showBack = false)
        body.gravity = Gravity.CENTER_HORIZONTAL

        val lang = chip(if (currentLanguage == "en") "English" else "Kannada") {
            currentLanguage = if (currentLanguage == "en") "kn" else "en"
            prefs.edit().putString(KEY_LANGUAGE, currentLanguage).apply()
            showRoleSelection()
        }
        body.addView(lang, LinearLayout.LayoutParams(-2, 44.dp()).withGravity(Gravity.END))

        addLogo(body, 112)

        body.addView(label(t("app_name"), 32f, bold = true).center())
        body.addView(label(t("tagline"), 16f, color = muted).center())
        body.addView(label(t("role_prompt"), 17f, bold = true).center().top(34))
        body.addView(primary(t("customer_role")) { showCustomerLogin() }.full().top(18))
        body.addView(secondary(t("manager_role")) { showManagerLogin() }.full().top(12))
    }

    private fun showCustomerLogin() {
        val body = screen(t("customer_login")) { showRoleSelection() }
        body.addView(secondary(t("back_to_roles")) { showRoleSelection() }.full())
        body.addView(section(t("welcome")))
        body.addView(label(t("phone_help"), 15f, color = muted))
        val phone = input(t("phone_hint"), InputType.TYPE_CLASS_PHONE)
        body.addView(phone.full().top(16))
        body.addView(primary(t("send_otp")) {
            val number = phone.text.toString().trim()
            if (!number.matches(Regex("\\d{10}"))) {
                toast(t("invalid_phone"))
                return@primary
            }
            customerPhone = number
            otp = Random.nextInt(100000, 999999).toString()
            toast("${t("otp_sent")} +91 $number: $otp")
            showOtp()
        }.full().top(14))
    }

    private fun showOtp() {
        val body = screen(t("verify_otp")) { showCustomerLogin() }
        body.addView(section(t("enter_otp")))
        body.addView(label("${t("otp_sent")} +91 $customerPhone", 15f, color = muted))
        val code = input(t("otp_hint"), InputType.TYPE_CLASS_NUMBER)
        body.addView(code.full().top(16))
        body.addView(primary(t("verify_continue")) {
            if (code.text.toString().trim() != otp) {
                toast(t("wrong_otp"))
                return@primary
            }
            prefs.edit().putString(KEY_CUSTOMER_PHONE, customerPhone).apply()
            showCustomerShell(Tab.HOME)
        }.full().top(14))
    }

    private fun showManagerLogin() {
        val body = screen(t("manager_login")) { showRoleSelection() }
        body.addView(secondary(t("back_to_roles")) { showRoleSelection() }.full())
        body.addView(section(t("manager_login")))
        body.addView(label("${t("manager_allowed")} $managerEmail", 14f, color = muted))
        val email = input(t("email"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val password = input(t("password"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        body.addView(email.full().top(14))
        body.addView(password.full().top(10))
        body.addView(primary(t("login")) {
            val value = email.text.toString().trim().lowercase(Locale.US)
            if (value != managerEmail || password.text.isBlank()) {
                toast(t("invalid_manager"))
                return@primary
            }
            prefs.edit().putString(KEY_MANAGER_EMAIL, value).apply()
            showManagerDashboard()
        }.full().top(14))
    }

    private fun showCustomerShell(tab: Tab) {
        customerPhone = prefs.getString(KEY_CUSTOMER_PHONE, customerPhone).orEmpty()
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
        }
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 14.dp(), 18.dp(), 22.dp())
        }
        scroll.addView(body)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(4.dp(), 8.dp(), 4.dp(), 10.dp())
            setBackgroundColor(panel)
        }
        Tab.entries.forEach { item ->
            nav.addView(navButton(item.label(), selected = item == tab) { showCustomerShell(item) }, LinearLayout.LayoutParams(0, 62.dp(), 1f))
        }
        outer.addView(nav)
        setContentView(outer)

        when (tab) {
            Tab.HOME -> renderHome(body)
            Tab.PLAYS -> renderPlays(body)
            Tab.BOOKINGS -> renderBookings(body)
            Tab.FAN_WALL -> renderFanWall(body)
            Tab.PROFILE -> renderProfile(body)
        }
    }

    private fun renderHome(body: LinearLayout) {
        body.addView(topTitle(t("tonight_shows"), now("EEEE, dd MMM yyyy")))
        body.addView(label(t("home_intro"), 15f, color = muted).withBottom(8))
        val shows = shows()
        if (shows.length() == 0) {
            body.addView(emptyState(t("no_shows")))
        }
        for (i in 0 until shows.length()) {
            body.addView(showCard(shows.getJSONObject(i), manager = false).top(12))
        }
    }

    private fun renderPlays(body: LinearLayout) {
        body.addView(topTitle(t("plays"), t("all_show_details")))
        val shows = shows()
        for (i in 0 until shows.length()) {
            val show = shows.getJSONObject(i)
            body.addView(card().apply {
                addView(label(show.optString("name"), 20f, bold = true))
                addView(label(show.optString("desc"), 14f, color = muted))
                addView(rowText(t("cast"), show.optString("actor")))
                addView(rowText(t("venue"), show.optString("venue")))
                addView(primary(t("view_details")) { showDetails(show) }.full().top(8))
            }.top(12))
        }
    }

    private fun renderBookings(body: LinearLayout) {
        body.addView(topTitle(t("my_bookings"), t("offline_saved")))
        var count = 0
        val all = bookings()
        for (i in all.length() - 1 downTo 0) {
            val booking = all.getJSONObject(i)
            if (booking.optString("phone") != customerPhone) continue
            count++
            body.addView(card().apply {
                addView(label(booking.optString("showName"), 19f, bold = true))
                addView(rowText(t("seats"), booking.optString("seats")))
                addView(rowText(t("total"), "Rs ${booking.optInt("total")}"))
                addView(rowText(t("booked_on"), booking.optString("date")))
                addView(label("QR: NM-${booking.optString("id").takeLast(6)}", 18f, bold = true, color = leaf).top(6))
            }.top(12))
        }
        if (count == 0) body.addView(emptyState(t("no_bookings")))
    }

    private fun renderFanWall(body: LinearLayout) {
        body.addView(topTitle(t("fan_wall"), t("fan_wall_subtitle")))
        val postText = input(t("fan_hint"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        body.addView(postText.full().top(8))
        body.addView(primary(t("post")) {
            val text = postText.text.toString().trim()
            if (text.isBlank()) return@primary
            val posts = fanPosts()
            posts.put(JSONObject().put("phone", customerPhone).put("text", text).put("date", now()))
            prefs.edit().putString(KEY_FAN_POSTS, posts.toString()).apply()
            showCustomerShell(Tab.FAN_WALL)
        }.full().top(10))

        val posts = fanPosts()
        if (posts.length() == 0) body.addView(emptyState(t("no_fan_posts")).top(14))
        for (i in posts.length() - 1 downTo 0) {
            val post = posts.getJSONObject(i)
            body.addView(card().apply {
                addView(label("+91 ${post.optString("phone").takeLast(4).padStart(10, '*')}", 14f, bold = true))
                addView(label(post.optString("text"), 16f))
                addView(label(post.optString("date"), 12f, color = muted))
            }.top(10))
        }
    }

    private fun renderProfile(body: LinearLayout) {
        body.addView(topTitle(t("profile"), t("profile_subtitle")))
        body.addView(card().apply {
            addView(label(t("customer_profile"), 19f, bold = true))
            addView(rowText(t("phone"), "+91 $customerPhone"))
            addView(rowText(t("language"), if (currentLanguage == "en") "English" else "Kannada"))
            addView(secondary(t("switch_language")) {
                currentLanguage = if (currentLanguage == "en") "kn" else "en"
                prefs.edit().putString(KEY_LANGUAGE, currentLanguage).apply()
                showCustomerShell(Tab.PROFILE)
            }.full().top(10))
        })
        body.addView(primary(t("logout")) {
            prefs.edit().remove(KEY_CUSTOMER_PHONE).apply()
            showRoleSelection()
        }.full().top(14))
    }

    private fun showDetails(show: JSONObject) {
        val body = screen(show.optString("name")) { showCustomerShell(Tab.HOME) }
        addPoster(body, show.optString("posterUri"), 230)
        body.addView(label(show.optString("name"), 25f, bold = true).top(10))
        body.addView(label("4.8 / 5 - 125 ${t("reviews")}", 14f, color = saffron))
        body.addView(card().apply {
            addView(rowText(t("date"), now("dd MMM yyyy")))
            addView(rowText(t("time"), "${show.optString("time")} | ${show.optString("duration")}"))
            addView(rowText(t("venue"), show.optString("venue")))
            addView(rowText(t("price"), "Rs ${show.optInt("price")}"))
            addView(rowText(t("available"), "${availableSeats(show)} / ${show.optInt("seats")}"))
        }.top(12))
        body.addView(section(t("about")))
        body.addView(label(show.optString("desc"), 15f, color = muted))
        body.addView(section(t("cast")))
        body.addView(card().apply {
            addActor(show.optString("actorUri"))
            addView(label(show.optString("actor"), 19f, bold = true).top(8))
            addView(label(show.optString("persona"), 15f, color = muted))
        }.top(8))
        body.addView(section(t("reviews")))
        body.addView(label(t("review_sample"), 15f, color = muted))
        body.addView(primary(t("book_seats")) { showSeatSelection(show) }.full().top(18))
    }

    private fun showSeatSelection(show: JSONObject) {
        val booked = bookedSeats(show.optString("id"))
        val selected = linkedSetOf<Int>()
        val body = screen(t("select_seats")) { showDetails(show) }
        body.addView(label(show.optString("name"), 21f, bold = true))
        body.addView(label("${t("price")}: Rs ${show.optInt("price")} ${t("per_seat")}", 15f, color = muted))

        val totalLabel = label("${t("total")}: Rs 0", 18f, bold = true, color = leaf).top(10)
        val grid = GridLayout(this).apply { columnCount = 5 }

        fun redraw() {
            grid.removeAllViews()
            for (seat in 1..show.optInt("seats")) {
                val stateColor = when {
                    booked.contains(seat) -> maroon
                    selected.contains(seat) -> leaf
                    else -> blue
                }
                val button = Button(this).apply {
                    text = seat.toString()
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(stateColor)
                    isEnabled = !booked.contains(seat)
                    setOnClickListener {
                        if (selected.contains(seat)) {
                            selected.remove(seat)
                        } else if (selected.size >= 10) {
                            toast(t("max_seats"))
                        } else {
                            selected.add(seat)
                        }
                        totalLabel.text = "${t("total")}: Rs ${selected.size * show.optInt("price")}"
                        redraw()
                    }
                }
                grid.addView(button, ViewGroup.LayoutParams(58.dp(), 48.dp()))
            }
        }

        redraw()
        body.addView(legend())
        body.addView(grid.top(12))
        body.addView(totalLabel)
        body.addView(primary(t("confirm_booking")) {
            if (selected.isEmpty()) {
                toast(t("select_one_seat"))
                return@primary
            }
            val latestBooked = bookedSeats(show.optString("id"))
            if (selected.any { latestBooked.contains(it) }) {
                toast(t("seat_conflict"))
                showSeatSelection(show)
                return@primary
            }
            AlertDialog.Builder(this)
                .setTitle(t("confirm_booking"))
                .setMessage("${selected.joinToString()} - Rs ${selected.size * show.optInt("price")}")
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("book")) { _, _ -> saveBooking(show, selected) }
                .show()
        }.full().top(16))
    }

    private fun saveBooking(show: JSONObject, seats: Set<Int>) {
        val all = bookings()
        all.put(JSONObject()
            .put("id", System.currentTimeMillis().toString())
            .put("showId", show.optString("id"))
            .put("showName", show.optString("name"))
            .put("phone", customerPhone)
            .put("seats", seats.joinToString())
            .put("total", seats.size * show.optInt("price"))
            .put("date", now()))
        prefs.edit().putString(KEY_BOOKINGS, all.toString()).apply()
        AlertDialog.Builder(this)
            .setTitle(t("booking_success"))
            .setMessage(t("ticket_saved"))
            .setPositiveButton(t("my_bookings")) { _, _ -> showCustomerShell(Tab.BOOKINGS) }
            .show()
    }

    private fun showManagerDashboard() {
        val body = screen(t("manager_dashboard")) { showRoleSelection() }
        val showCount = shows().length()
        val bookingCount = bookings().length()
        val totalSeats = totalSeatCount()
        val bookedSeats = totalBookedSeatCount()
        body.addView(topTitle(t("manager_dashboard"), "${showCount} ${t("shows")} | ${bookingCount} ${t("bookings")}"))
        body.addView(card().apply {
            addView(label(t("seat_summary"), 19f, bold = true))
            addView(rowText(t("total_seats"), totalSeats.toString()))
            addView(rowText(t("booked_seats"), bookedSeats.toString()))
            addView(rowText(t("available_seats"), (totalSeats - bookedSeats).coerceAtLeast(0).toString()))
        }.top(12))
        body.addView(primary(t("add_show")) { showShowForm(null) }.full().top(10))
        val all = shows()
        if (all.length() == 0) body.addView(emptyState(t("no_shows")).top(12))
        for (i in 0 until all.length()) {
            body.addView(showCard(all.getJSONObject(i), manager = true).top(12))
        }
    }

    private fun showShowForm(existing: JSONObject?) {
        val editing = existing != null
        posterUri = existing?.optString("posterUri").orEmpty()
        actorUri = existing?.optString("actorUri").orEmpty()

        val body = screen(if (editing) t("edit_show") else t("add_show")) { showManagerDashboard() }
        val name = input(t("show_name"), InputType.TYPE_CLASS_TEXT).fill(existing, "name")
        val venue = input(t("venue"), InputType.TYPE_CLASS_TEXT).fill(existing, "venue")
        val time = input(t("time_hint"), InputType.TYPE_CLASS_TEXT).fill(existing, "time")
        val duration = input(t("duration_hint"), InputType.TYPE_CLASS_TEXT).fill(existing, "duration")
        val price = input(t("price"), InputType.TYPE_CLASS_NUMBER).fill(existing, "price")
        val seats = input(t("total_seats"), InputType.TYPE_CLASS_NUMBER).fill(existing, "seats")
        val actor = input(t("lead_actor"), InputType.TYPE_CLASS_TEXT).fill(existing, "actor")
        val persona = input(t("persona"), InputType.TYPE_CLASS_TEXT).fill(existing, "persona")
        val desc = input(t("description"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE).fill(existing, "desc")

        listOf(name, venue, time, duration, price, seats, actor, persona, desc).forEach { body.addView(it.full().top(10)) }
        body.addView(secondary(if (posterUri.isBlank()) t("upload_poster") else t("change_poster")) {
            pendingImageTarget = "poster"
            pickImage()
        }.full().top(12))
        body.addView(label(if (posterUri.isBlank()) t("poster_required") else t("poster_selected"), 13f, color = muted))
        body.addView(secondary(if (actorUri.isBlank()) t("upload_actor") else t("change_actor")) {
            pendingImageTarget = "actor"
            pickImage()
        }.full().top(8))
        body.addView(label(if (actorUri.isBlank()) t("actor_optional") else t("actor_selected"), 13f, color = muted))

        body.addView(primary(t("save_show")) {
            val totalSeats = seats.text.toString().toIntOrNull() ?: 0
            if (name.text.isBlank() || venue.text.isBlank() || time.text.isBlank() || totalSeats <= 0) {
                toast(t("fill_required"))
                return@primary
            }
            val id = existing?.optString("id")?.takeIf { it.isNotBlank() } ?: System.currentTimeMillis().toString()
            val saved = JSONObject()
                .put("id", id)
                .put("name", name.text.toString().trim())
                .put("venue", venue.text.toString().trim())
                .put("time", time.text.toString().trim())
                .put("duration", duration.text.toString().ifBlank { "3 hours" })
                .put("price", max(1, price.text.toString().toIntOrNull() ?: 50))
                .put("seats", totalSeats)
                .put("actor", actor.text.toString().ifBlank { "Drama troupe" })
                .put("persona", persona.text.toString().ifBlank { "Lead role" })
                .put("desc", desc.text.toString().ifBlank { t("default_desc") })
                .put("posterUri", posterUri)
                .put("actorUri", actorUri)
                .put("managerEmail", managerEmail)

            val next = JSONArray()
            val all = shows()
            for (i in 0 until all.length()) {
                val item = all.getJSONObject(i)
                if (item.optString("id") != id) next.put(item)
            }
            next.put(saved)
            prefs.edit().putString(KEY_SHOWS, next.toString()).apply()
            toast(t("show_saved"))
            showManagerDashboard()
        }.full().top(16))
    }

    @Deprecated("startActivityForResult keeps this project dependency-free.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != IMAGE_REQUEST || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {
        }
        if (pendingImageTarget == "poster") posterUri = uri.toString() else actorUri = uri.toString()
        toast(t("image_selected"))
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun showCard(show: JSONObject, manager: Boolean): LinearLayout {
        return card().apply {
            addPoster(this, show.optString("posterUri"), 174)
            addView(label(show.optString("name"), 21f, bold = true).top(8))
            addView(label("${show.optString("time")} | ${show.optString("duration")}", 14f, color = muted))
            addView(label(show.optString("venue"), 14f, color = muted))
            addView(label("${t("available")}: ${availableSeats(show)}", 14f, color = seatColor(availableSeats(show))))
            addView(label("Rs ${show.optInt("price")} | 4.8 ${t("rating")}", 14f, color = saffron))
            addView(label("${t("lead")}: ${show.optString("actor")} (${show.optString("persona")})", 14f, color = muted))
            if (manager) {
                val bookedForShow = bookedSeats(show.optString("id")).size
                addView(card().apply {
                    setPadding(10.dp(), 10.dp(), 10.dp(), 10.dp())
                    addView(rowText(t("booked_seats"), bookedForShow.toString()))
                    addView(rowText(t("available_seats"), availableSeats(show).toString()))
                }.top(8))
                addView(secondary(t("edit")) { showShowForm(show) }.full().top(8))
                addView(textButton(t("delete")) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(t("delete_show"))
                        .setMessage(show.optString("name"))
                        .setNegativeButton(t("cancel"), null)
                        .setPositiveButton(t("delete")) { _, _ -> deleteShow(show.optString("id")) }
                        .show()
                }.full().top(4))
            } else {
                addView(primary(t("book_now")) { showSeatSelection(show) }.full().top(8))
                addView(textButton(t("view_details")) { showDetails(show) }.full().top(2))
            }
        }
    }

    private fun deleteShow(id: String) {
        val next = JSONArray()
        val all = shows()
        for (i in 0 until all.length()) {
            val show = all.getJSONObject(i)
            if (show.optString("id") != id) next.put(show)
        }
        prefs.edit().putString(KEY_SHOWS, next.toString()).apply()
        toast(t("show_deleted"))
        showManagerDashboard()
    }

    private fun screen(title: String, showBack: Boolean = true, back: (() -> Unit)? = null): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
        }
        val appBar = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10.dp(), 10.dp(), 12.dp(), 8.dp())
        }
        if (showBack && back != null) {
            appBar.addView(textButton(t("back")) { back() }, LinearLayout.LayoutParams(88.dp(), 44.dp()))
        }
        appBar.addView(label(title, 22f, bold = true), LinearLayout.LayoutParams(0, -2, 1f))
        outer.addView(appBar)

        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 8.dp(), 18.dp(), 24.dp())
        }
        scroll.addView(body)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(outer)
        return body
    }

    private fun topTitle(title: String, subtitle: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(label(title, 26f, bold = true))
        addView(label(subtitle, 14f, color = muted))
    }

    private fun addLogo(parent: LinearLayout, sizeDp: Int) {
        parent.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(maroon, 26.dp(), saffron)
            addView(TextView(this@MainActivity).apply {
                text = "NM"
                textSize = 34f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
            }, LinearLayout.LayoutParams(-1, 0, 1f))
            addView(TextView(this@MainActivity).apply {
                text = t("drama_booking")
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(leaf)
            }, LinearLayout.LayoutParams(-1, 26.dp()))
        }, LinearLayout.LayoutParams(sizeDp.dp(), sizeDp.dp()).withTop(26).withBottom(18))
    }

    private fun section(text: String) = label(text, 20f, bold = true).top(18)

    private fun emptyState(text: String) = card().apply {
        gravity = Gravity.CENTER
        addView(label(text, 16f, color = muted).center())
    }

    private fun rowText(left: String, right: String) = TextView(this).apply {
        text = "$left: $right"
        textSize = 15f
        setTextColor(ink)
        setPadding(0, 4.dp(), 0, 4.dp())
    }

    private fun legend() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 12.dp(), 0, 0)
        listOf(t("available") to blue, t("selected") to leaf, t("booked") to maroon).forEach { (name, color) ->
            addView(TextView(this@MainActivity).apply {
                text = "  $name  "
                textSize = 12f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = rounded(color, 18.dp())
            }, LinearLayout.LayoutParams(0, 30.dp(), 1f).withMargin(4))
        }
    }

    private fun addPoster(parent: LinearLayout, uri: String, heightDp: Int) {
        parent.addView(ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(Color.rgb(242, 231, 214), 10.dp())
            if (uri.isNotBlank()) setImageURI(Uri.parse(uri)) else setImageResource(android.R.drawable.ic_menu_gallery)
        }, LinearLayout.LayoutParams(-1, heightDp.dp()).withBottom(8))
    }

    private fun LinearLayout.addActor(uri: String) {
        addView(ImageView(this@MainActivity).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(Color.rgb(242, 231, 214), 48.dp())
            if (uri.isNotBlank()) setImageURI(Uri.parse(uri)) else setImageResource(android.R.drawable.ic_menu_myplaces)
        }, LinearLayout.LayoutParams(86.dp(), 86.dp()).withGravity(Gravity.CENTER_HORIZONTAL))
    }

    private fun input(hint: String, type: Int) = EditText(this).apply {
        this.hint = hint
        inputType = type
        textSize = 16f
        setTextColor(ink)
        setHintTextColor(muted)
        setPadding(14.dp(), 10.dp(), 14.dp(), 10.dp())
        background = rounded(panel, 8.dp())
    }

    private fun primary(text: String, click: () -> Unit) = Button(this).apply {
        this.text = text
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        setBackgroundColor(maroon)
        setOnClickListener { click() }
    }

    private fun secondary(text: String, click: () -> Unit) = Button(this).apply {
        this.text = text
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        setBackgroundColor(leaf)
        setOnClickListener { click() }
    }

    private fun textButton(text: String, click: () -> Unit) = Button(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(ink)
        setBackgroundColor(Color.TRANSPARENT)
        setOnClickListener { click() }
    }

    private fun chip(text: String, click: () -> Unit) = Button(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(maroon)
        background = rounded(Color.TRANSPARENT, 22.dp(), maroon)
        setOnClickListener { click() }
    }

    private fun navButton(text: String, selected: Boolean, click: () -> Unit) = Button(this).apply {
        this.text = text
        textSize = 10f
        isAllCaps = false
        typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        setTextColor(if (selected) Color.WHITE else ink)
        background = rounded(if (selected) maroon else Color.TRANSPARENT, 12.dp(), if (selected) maroon else border)
        setOnClickListener { click() }
    }

    private fun label(text: String, size: Float = 15f, bold: Boolean = false, color: Int = ink) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        includeFontPadding = true
        setPadding(0, 4.dp(), 0, 4.dp())
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(14.dp(), 14.dp(), 14.dp(), 14.dp())
        background = rounded(panel, 10.dp())
    }

    private fun EditText.fill(source: JSONObject?, key: String): EditText {
        val value = source?.opt(key)?.toString().orEmpty()
        setText(value)
        return this
    }

    private fun <T : View> T.full() = this.apply { layoutParams = LinearLayout.LayoutParams(-1, -2) }
    private fun <T : View> T.top(dp: Int) = this.apply { (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp.dp() }
    private fun <T : View> T.withBottom(dp: Int) = this.apply { (layoutParams as? LinearLayout.LayoutParams)?.bottomMargin = dp.dp() }
    private fun TextView.center() = this.apply { gravity = Gravity.CENTER }

    private fun LinearLayout.LayoutParams.withTop(dp: Int) = apply { topMargin = dp.dp() }
    private fun LinearLayout.LayoutParams.withBottom(dp: Int) = apply { bottomMargin = dp.dp() }
    private fun LinearLayout.LayoutParams.withMargin(dp: Int) = apply {
        leftMargin = dp.dp()
        rightMargin = dp.dp()
    }
    private fun LinearLayout.LayoutParams.withGravity(value: Int) = apply { gravity = value }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int, stroke: Int = border) =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            setStroke(1.dp(), stroke)
        }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    private fun now(pattern: String = "dd MMM yyyy, hh:mm a") = SimpleDateFormat(pattern, Locale.US).format(Date())
    private fun shows() = JSONArray(prefs.getString(KEY_SHOWS, "[]") ?: "[]")
    private fun bookings() = JSONArray(prefs.getString(KEY_BOOKINGS, "[]") ?: "[]")
    private fun fanPosts() = JSONArray(prefs.getString(KEY_FAN_POSTS, "[]") ?: "[]")
    private fun availableSeats(show: JSONObject) = show.optInt("seats") - bookedSeats(show.optString("id")).size
    private fun totalSeatCount(): Int {
        val all = shows()
        var total = 0
        for (i in 0 until all.length()) total += all.getJSONObject(i).optInt("seats")
        return total
    }

    private fun totalBookedSeatCount(): Int {
        val all = shows()
        var total = 0
        for (i in 0 until all.length()) total += bookedSeats(all.getJSONObject(i).optString("id")).size
        return total
    }

    private fun seatColor(count: Int) = when {
        count > 20 -> leaf
        count >= 5 -> saffron
        else -> maroon
    }

    private fun bookedSeats(showId: String): Set<Int> {
        val result = mutableSetOf<Int>()
        val all = bookings()
        for (i in 0 until all.length()) {
            val booking = all.getJSONObject(i)
            if (booking.optString("showId") == showId) {
                booking.optString("seats").split(",").mapNotNull { it.trim().toIntOrNull() }.forEach(result::add)
            }
        }
        return result
    }

    private fun seedDemoData() {
        if (prefs.contains(KEY_SHOWS)) return
        val demo = JSONArray()
            .put(JSONObject()
                .put("id", "demo_1")
                .put("name", "Veera Abhimanyu")
                .put("venue", "Village Open Stage")
                .put("time", "7:00 PM")
                .put("duration", "3 hours")
                .put("price", 80)
                .put("seats", 40)
                .put("actor", "Ravi Kumar")
                .put("persona", "Abhimanyu")
                .put("desc", "A mythological stage drama with live music, expressive dialogue, and a powerful family story.")
                .put("posterUri", "")
                .put("actorUri", "")
                .put("managerEmail", managerEmail))
            .put(JSONObject()
                .put("id", "demo_2")
                .put("name", "Sri Krishna Sandhana")
                .put("venue", "Town Hall Ground")
                .put("time", "9:00 PM")
                .put("duration", "4 hours")
                .put("price", 100)
                .put("seats", 60)
                .put("actor", "Meena Patil")
                .put("persona", "Draupadi")
                .put("desc", "A traditional night performance about courage, dharma, and the choices before war.")
                .put("posterUri", "")
                .put("actorUri", "")
                .put("managerEmail", managerEmail))
        prefs.edit().putString(KEY_SHOWS, demo.toString()).apply()
    }

    private fun t(key: String): String {
        val en = mapOf(
            "app_name" to "Namma-Mela",
            "drama_booking" to "Drama Booking",
            "back" to "Back",
            "back_to_roles" to "Back to Role Selection",
            "nav_home" to "Home Page",
            "nav_plays" to "Upcoming Plays",
            "nav_bookings" to "My Bookings",
            "nav_fan" to "Fan Page",
            "nav_profile" to "Profile",
            "tagline" to "Bringing local drama to life",
            "role_prompt" to "Choose how you want to continue",
            "customer_role" to "I am a Customer",
            "manager_role" to "I am a Manager",
            "customer_login" to "Customer Login",
            "welcome" to "Welcome to Namma-Mela",
            "phone_help" to "Enter your mobile number. We use OTP verification for customers.",
            "phone_hint" to "10 digit mobile number",
            "send_otp" to "Send OTP",
            "invalid_phone" to "Enter a valid 10 digit number",
            "otp_sent" to "OTP sent to",
            "verify_otp" to "Verify OTP",
            "enter_otp" to "Enter OTP",
            "otp_hint" to "6 digit OTP",
            "verify_continue" to "Verify and Continue",
            "wrong_otp" to "Wrong OTP",
            "manager_login" to "Manager Login",
            "manager_allowed" to "Manager email:",
            "email" to "Email",
            "password" to "Password",
            "login" to "Login",
            "invalid_manager" to "Enter the approved manager email and a password",
            "tonight_shows" to "Tonight's Shows",
            "home_intro" to "Browse shows, view details, and book seats from your phone.",
            "no_shows" to "No shows available yet.",
            "plays" to "Plays",
            "all_show_details" to "All listed drama shows",
            "bookings" to "bookings",
            "my_bookings" to "My Bookings",
            "offline_saved" to "Tickets are saved on this device",
            "no_bookings" to "No bookings yet.",
            "fan_wall" to "Fan Wall",
            "fan_wall_subtitle" to "Share your thoughts with other drama fans",
            "fan_hint" to "Write a short message",
            "post" to "Post",
            "no_fan_posts" to "No fan posts yet.",
            "profile" to "Profile",
            "profile_subtitle" to "Your app preferences",
            "customer_profile" to "Customer Profile",
            "phone" to "Phone",
            "language" to "Language",
            "switch_language" to "Switch Language",
            "logout" to "Logout",
            "view_details" to "View Details",
            "cast" to "Cast",
            "venue" to "Venue",
            "date" to "Date",
            "time" to "Time",
            "price" to "Price",
            "available" to "Available",
            "about" to "About",
            "reviews" to "reviews",
            "review_sample" to "Audience loved the music, clear dialogue, and emotional scenes.",
            "book_seats" to "Book Seats",
            "select_seats" to "Select Seats",
            "per_seat" to "per seat",
            "total" to "Total",
            "selected" to "Selected",
            "booked" to "Booked",
            "confirm_booking" to "Confirm Booking",
            "select_one_seat" to "Select at least one seat",
            "max_seats" to "You can book up to 10 seats at a time",
            "seat_conflict" to "One selected seat was already booked",
            "cancel" to "Cancel",
            "book" to "Book",
            "booking_success" to "Booking Successful",
            "ticket_saved" to "Your ticket has been saved in My Bookings.",
            "seats" to "Seats",
            "booked_on" to "Booked on",
            "manager_dashboard" to "Manager Dashboard",
            "seat_summary" to "Seat Summary",
            "booked_seats" to "Booked seats",
            "available_seats" to "Available seats",
            "remaining_seats" to "Remaining seats",
            "shows" to "shows",
            "add_show" to "Add Show",
            "edit_show" to "Edit Show",
            "show_name" to "Drama name",
            "time_hint" to "Time, e.g. 7:00 PM",
            "duration_hint" to "Duration, e.g. 3 hours",
            "total_seats" to "Total seats",
            "lead_actor" to "Lead actor",
            "persona" to "Character / persona",
            "description" to "About the show",
            "upload_poster" to "Upload Poster",
            "change_poster" to "Change Poster",
            "poster_required" to "Poster optional for demo, recommended for real shows",
            "poster_selected" to "Poster selected",
            "upload_actor" to "Upload Actor Photo",
            "change_actor" to "Change Actor Photo",
            "actor_optional" to "Actor photo optional",
            "actor_selected" to "Actor photo selected",
            "save_show" to "Save Show",
            "fill_required" to "Fill name, venue, time and total seats",
            "default_desc" to "A traditional drama performance for the local audience.",
            "show_saved" to "Show saved",
            "image_selected" to "Image selected. Save the show to keep it.",
            "book_now" to "Book Now",
            "rating" to "rating",
            "lead" to "Lead",
            "edit" to "Edit",
            "delete" to "Delete",
            "delete_show" to "Delete show?",
            "show_deleted" to "Show deleted"
        )
        val kn = mapOf(
            "app_name" to "ನಮ್ಮ ಮೇಳ",
            "tagline" to "ಸ್ಥಳೀಯ ನಾಟಕವನ್ನು ಜನರಿಗೆ ಹತ್ತಿರ ತರುತ್ತೇವೆ",
            "role_prompt" to "ನೀವು ಹೇಗೆ ಮುಂದುವರಿಯಬೇಕು?",
            "customer_role" to "ನಾನು ಗ್ರಾಹಕ",
            "manager_role" to "ನಾನು ಮ್ಯಾನೇಜರ್",
            "customer_login" to "ಗ್ರಾಹಕ ಲಾಗಿನ್",
            "welcome" to "ನಮ್ಮ ಮೇಳಕ್ಕೆ ಸ್ವಾಗತ",
            "phone_help" to "ನಿಮ್ಮ ಮೊಬೈಲ್ ಸಂಖ್ಯೆಯನ್ನು ನಮೂದಿಸಿ. ಗ್ರಾಹಕರಿಗೆ OTP ಪರಿಶೀಲನೆ ಇದೆ.",
            "phone_hint" to "10 ಅಂಕಿಯ ಮೊಬೈಲ್ ಸಂಖ್ಯೆ",
            "send_otp" to "OTP ಕಳುಹಿಸಿ",
            "invalid_phone" to "ಸರಿಯಾದ 10 ಅಂಕಿಯ ಸಂಖ್ಯೆಯನ್ನು ನಮೂದಿಸಿ",
            "otp_sent" to "OTP ಕಳುಹಿಸಲಾಗಿದೆ",
            "verify_otp" to "OTP ಪರಿಶೀಲನೆ",
            "enter_otp" to "OTP ನಮೂದಿಸಿ",
            "otp_hint" to "6 ಅಂಕಿಯ OTP",
            "verify_continue" to "ಪರಿಶೀಲಿಸಿ ಮುಂದುವರಿಸಿ",
            "wrong_otp" to "OTP ತಪ್ಪಾಗಿದೆ",
            "manager_login" to "ಮ್ಯಾನೇಜರ್ ಲಾಗಿನ್",
            "manager_allowed" to "ಮ್ಯಾನೇಜರ್ ಇಮೇಲ್:",
            "email" to "ಇಮೇಲ್",
            "password" to "ಪಾಸ್ವರ್ಡ್",
            "login" to "ಲಾಗಿನ್",
            "invalid_manager" to "ಅನುಮೋದಿತ ಇಮೇಲ್ ಮತ್ತು ಪಾಸ್ವರ್ಡ್ ನಮೂದಿಸಿ",
            "tonight_shows" to "ಇಂದಿನ ಪ್ರದರ್ಶನಗಳು",
            "home_intro" to "ಪ್ರದರ್ಶನಗಳನ್ನು ನೋಡಿ, ವಿವರ ತಿಳಿದು, ಆಸನಗಳನ್ನು ಬುಕ್ ಮಾಡಿ.",
            "no_shows" to "ಇನ್ನೂ ಪ್ರದರ್ಶನಗಳಿಲ್ಲ.",
            "plays" to "ನಾಟಕಗಳು",
            "all_show_details" to "ಎಲ್ಲಾ ಪ್ರದರ್ಶನಗಳ ವಿವರ",
            "my_bookings" to "ನನ್ನ ಬುಕ್ಕಿಂಗ್",
            "fan_wall" to "ಅಭಿಮಾನಿ ಗೋಡೆ",
            "profile" to "ಪ್ರೊಫೈಲ್",
            "book_now" to "ಈಗ ಬುಕ್ ಮಾಡಿ",
            "view_details" to "ವಿವರ ನೋಡಿ",
            "book_seats" to "ಆಸನ ಬುಕ್ ಮಾಡಿ",
            "select_seats" to "ಆಸನ ಆಯ್ಕೆ",
            "confirm_booking" to "ಬುಕ್ಕಿಂಗ್ ದೃಢೀಕರಿಸಿ",
            "booking_success" to "ಬುಕ್ಕಿಂಗ್ ಯಶಸ್ವಿ",
            "manager_dashboard" to "ಮ್ಯಾನೇಜರ್ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್",
            "add_show" to "ಪ್ರದರ್ಶನ ಸೇರಿಸಿ",
            "edit_show" to "ಪ್ರದರ್ಶನ ತಿದ್ದು",
            "save_show" to "ಪ್ರದರ್ಶನ ಉಳಿಸಿ",
            "logout" to "ಲಾಗೌಟ್",
            "delete" to "ಅಳಿಸಿ",
            "edit" to "ತಿದ್ದು"
        )
        return if (currentLanguage == "kn") kn[key] ?: en[key] ?: key else en[key] ?: key
    }

    private fun Tab.label(): String = when (this) {
        Tab.HOME -> t("nav_home")
        Tab.PLAYS -> t("nav_plays")
        Tab.BOOKINGS -> t("nav_bookings")
        Tab.FAN_WALL -> t("nav_fan")
        Tab.PROFILE -> t("nav_profile")
    }

    private enum class Tab { HOME, PLAYS, BOOKINGS, FAN_WALL, PROFILE }

    companion object {
        private const val IMAGE_REQUEST = 71
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CUSTOMER_PHONE = "customer_phone"
        private const val KEY_MANAGER_EMAIL = "manager_email"
        private const val KEY_SHOWS = "shows"
        private const val KEY_BOOKINGS = "bookings"
        private const val KEY_FAN_POSTS = "fan_posts"
    }
}
