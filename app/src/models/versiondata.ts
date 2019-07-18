export interface VersionData {
    type: string,
    version: string
}

export function isVersionData(arg: any) : arg is VersionData {
    return arg.type !== undefined && arg.type == "version";
}
