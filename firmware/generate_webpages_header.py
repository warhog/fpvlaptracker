import os
filedata = {}

def open_file_and_convert(filename):
    print('read file {}'.format(filename))
    file = os.path.splitext(os.path.basename(filename))[0]

    with open (filename, 'r') as myfile:
        data = myfile.read()

    filedata[file] = data


open_file_and_convert('html/header.html')
open_file_and_convert('html/footer.html')
open_file_and_convert('html/index.html')
open_file_and_convert('html/wifi.html')

data = ''
print('generate header file')
for file in filedata:
    data = data + '\nconstexpr static char const *{} = R"(\n{})";\n'.format(file, filedata[file])

data = '#pragma once\nnamespace comm {{\nclass WifiWebServerFiles {{\n\npublic:{}\n}};\n}}'.format(data)

with open("webui-generated.h", "w") as header:
    print(data, file=header)

print('done')