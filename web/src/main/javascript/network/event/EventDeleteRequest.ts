import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import jqXHR = JQuery.jqXHR;

export class EventDeleteRequest implements AsyncTask {
	private readonly guildId: string;
	private readonly calNum: number;
	private readonly eventId: string;

	readonly callback: TaskCallback;

	apiKey: string = "";
	apiUrl: string = "";

	constructor(guildId: string, calNum: number, eventId: string, callback: TaskCallback) {
		this.guildId = guildId;
		this.calNum = calNum;
		this.eventId = eventId;
		this.callback = callback;
	}

	provideApiDetails(apiKey: string, apiUrl: string): void {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
	}


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
            "calendar_number": this.calNum,
            "event_id": this.eventId
        };

        $.ajax({
            url: this.apiUrl + "/v2/events/delete",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.EVENT_DELETE);
                status.code = 200;
                status.body = json;
                status.message = json.message;

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.EVENT_DELETE);
                status.code = jqXHR.status;
                status.body = jqXHR.responseJSON;
                status.message = jqXHR.responseJSON.message;

                this.onComplete(status);
            }.bind(this)
        });
    }

    onComplete(status: NetworkCallStatus): void {
        this.callback.onCallback(status);
    }
}