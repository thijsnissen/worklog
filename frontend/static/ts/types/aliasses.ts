import type { components, paths } from "./api.d.ts";

export type GetAllResponse = paths["/worklogs"]["get"]["responses"]["200"]["content"]["application/json"];

export type ImportInRangeStartInclusive = paths["/worklogs"]["post"]["parameters"]["query"]["startInclusive"];
export type ImportInRangeEndInclusive = paths["/worklogs"]["post"]["parameters"]["query"]["startInclusive"];
export type ImportInRangeResponse = paths["/worklogs"]["post"]["responses"]["200"]["content"]["application/json"];

export type ExportByIdsRequest = paths["/worklogs/export"]["post"]["requestBody"]["content"]["application/json"];
export type ExportByIdsResponse = paths["/worklogs/export"]["post"]["responses"]["200"]["content"]["application/json"];

export type DeleteByIdsRequest = paths["/worklogs/delete"]["post"]["requestBody"]["content"]["application/json"];
export type DeleteByIdsResponse = paths["/worklogs/delete"]["post"]["responses"]["200"]["content"]["application/json"];

export type Worklog = components["schemas"]["Worklog"];
export type HttpError = components["schemas"]["HttpError"];
