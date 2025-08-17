import { Worklog } from "./aliasses";

export type ApiRequestOptions = { method: string; headers?: Record<string, string>; body?: string };

export type MessageType = "error" | "success";

export type Direction = "asc" | "desc";

export type WorklogsTable = {
    data: Worklog[];
    sort: keyof Worklog;
    direction: Direction;
};

export type TimeoutId = {
    timeoutId: number;
};

export type StateHandler<T extends object> = {
    getState: () => Readonly<T>;
    setState: (state: Partial<T>) => void;
};
