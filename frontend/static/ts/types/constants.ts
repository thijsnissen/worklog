import { Worklog } from "./aliasses.js";
import { WorklogsTable } from "./types";

export const API_BASE_URL = "/api";

export const apiRequestUrls: Readonly<Record<string, { path: string; method: string }>> = {
    getAll: {
        path: "/worklogs",
        method: "GET",
    },
    importInRange: {
        path: "/worklogs",
        method: "POST",
    },
    exportByIds: {
        path: "/worklogs/export",
        method: "POST",
    },
    deleteByIds: {
        path: "/worklogs/delete",
        method: "POST",
    },
    flush: {
        path: "/worklogs",
        method: "DELETE",
    },
};

export const DEFAULT_WORKLOGS_TABLE: WorklogsTable = {
    data: [],
    sort: "startInclusive",
    direction: "asc",
};

export const WORKLOGS_SORTABLE_KEYS: (keyof Worklog)[] = [
    "id",
    "issueId",
    "issueKey",
    "startInclusive",
    "endInclusive",
    "durationSeconds",
    "description",
    "exported",
];

export const WORKLOGS_TABLE_ID: string = "worklogsTable";
export const WORKLOGS_TABLE_BODY_ID: string = "tableBody";
export const WORKLOGS_TABLE_ROW_ID: string = "tableRow";
export const NO_DATA_MESSAGE_ID: string = "noDataMessage";

export const IMPORT_IN_RANGE_BUTTON_ID: string = "importInRangeButton";
export const EXPORT_BY_IDS_BUTTON_ID: string = "exportByIdsButton";
export const DELETE_BUTTON_ID: string = "deleteButton";
export const PRINT_BUTTON_ID: string = "printButton";

export const START_DATE_TIME_INCLUSIVE_ID: string = "startDateTimeInclusive";
export const END_DATE_TIME_INCLUSIVE_ID: string = "endDateTimeInclusive";

export const MESSAGE_BOX_ID: string = "message";
export const SUMMARY_ID: string = "summary";
