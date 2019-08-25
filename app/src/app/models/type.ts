export interface TypeData {
    type: string
}

export function getDataType(data: string): string {
    try {
        let arg: TypeData = JSON.parse(data);
        return arg.type;
    } catch(ex) {
        console.log('exception during json.parse: ', ex, data);
    }
    return null;
}