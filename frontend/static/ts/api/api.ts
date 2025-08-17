import {
    DeleteByIdsRequest,
    DeleteByIdsResponse,
    ExportByIdsRequest,
    ExportByIdsResponse,
    GetAllResponse,
    HttpError,
    ImportInRangeEndInclusive,
    ImportInRangeResponse,
    ImportInRangeStartInclusive,
    Worklog,
} from "../types/aliasses.js";
import { API_BASE_URL, apiRequestUrls } from "../types/constants.js";
import { ApiRequestOptions } from "../types/types.js";

export async function getAll(): Promise<Worklog[]> {
    const result: GetAllResponse = await apiRequest(`${API_BASE_URL}${apiRequestUrls["getAll"].path}`);

    return result.worklogs;
}

export async function importInRange(
    startInclusive: ImportInRangeStartInclusive,
    endInclusive: ImportInRangeEndInclusive,
): Promise<number> {
    const url: string = `${API_BASE_URL}/worklogs?startInclusive=${encodeURIComponent(startInclusive)}&endInclusive=${encodeURIComponent(endInclusive)}`;
    const options: ApiRequestOptions = { method: "POST" };
    const result: ImportInRangeResponse = await apiRequest(url, options);

    return result.rowsAffected;
}

export async function exportByIds(ids: string[]): Promise<number> {
    const request: ExportByIdsRequest = { ids: ids };
    const url: string = `${API_BASE_URL}${apiRequestUrls["exportByIds"].path}`;
    const options: ApiRequestOptions = {
        method: apiRequestUrls["exportByIds"].method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request),
    };
    const response: ExportByIdsResponse = await apiRequest(url, options);

    return response.rowsAffected;
}

export async function deleteByIds(ids: string[]): Promise<number> {
    const request: DeleteByIdsRequest = { ids: ids };
    const response: DeleteByIdsResponse = await apiRequest(`${API_BASE_URL}${apiRequestUrls["deleteByIds"].path}`, {
        method: apiRequestUrls["deleteByIds"].method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request),
    });

    return response.rowsAffected;
}

export async function flush(): Promise<void> {
    const url: string = `${API_BASE_URL}${apiRequestUrls["flush"].path}`;
    const options: ApiRequestOptions = {
        method: apiRequestUrls["flush"].method,
    };

    await apiRequest(url, options);
}

async function apiRequest<T>(url: string, options?: ApiRequestOptions): Promise<T> {
    const response = await fetch(url, options);

    if (!response.ok) {
        const json = (await response.json()) as HttpError;
        const message = json.errors.map((e) => `\n${e.field}: ${e.message}`).join();

        throw new Error(message ? `${json.message}:${message.length && message}` : `HTTP ${response.status}`);
    }

    if (response.status == 204) {
        return undefined as T;
    }

    return (await response.json()) as T;
}
