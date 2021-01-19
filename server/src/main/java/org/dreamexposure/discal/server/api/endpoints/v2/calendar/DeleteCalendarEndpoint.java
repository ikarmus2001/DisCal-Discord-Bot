package org.dreamexposure.discal.server.api.endpoints.v2.calendar;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.CalendarUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/calendar")
public class DeleteCalendarEndpoint {
    @PostMapping(value = "/delete", produces = "application/json")
    public String deleteCalendar(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        } else if (authState.getReadOnly()) {
            response.setStatus(GlobalConst.STATUS_AUTHORIZATION_DENIED);
            response.setContentType("application/json");
            return JsonUtils.getJsonResponseMessage("Read-Only key not Allowed");
        }

        //Okay, now handle actual request.
        try {
            final JSONObject jsonMain = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));
            final int calNumber = jsonMain.getInt("calendar_number");

            final GuildSettings settings = DatabaseManager.getSettings(guildId).block();
            final CalendarData calendar = DatabaseManager.getCalendar(guildId, calNumber).block();

            if (!"primary".equalsIgnoreCase(calendar.getCalendarAddress())) {
                if (CalendarUtils.calendarExists(calendar, settings).block()) {
                    if (CalendarUtils.deleteCalendar(calendar, settings).block()) {
                        response.setContentType("application/json");
                        response.setStatus(GlobalConst.STATUS_SUCCESS);
                        return JsonUtils.getJsonResponseMessage("Calendar successfully deleted");
                    }
                    response.setContentType("application/json");
                    response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
                    return JsonUtils.getJsonResponseMessage("Internal Server Error");
                }
            }
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_NOT_FOUND);
            return JsonUtils.getJsonResponseMessage("Calendar not found");
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject.forException("[API-v2]", "Delete cal err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}

