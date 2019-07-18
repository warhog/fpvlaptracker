export interface TypeData {
    type: string
}

export function getDataType(data: string): string {
    let arg: TypeData = JSON.parse(data);
    return arg.type;
}