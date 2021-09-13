// https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API/File_drag_and_drop
function handleDrop(event, handler) {
    // Prevent default behavior (Prevent file from being opened)
    event.preventDefault();

    if (event.dataTransfer.items) {
        // Use DataTransferItemList interface to access the file(s)
        for (let i = 0; i < event.dataTransfer.items.length; i++) {
            // If dropped items aren't files, reject them
            if (event.dataTransfer.items[i].kind === 'file') {
                let file = event.dataTransfer.items[i].getAsFile();
                handler(file);
            }
        }
    } else {
        // Use DataTransfer interface to access the file(s)
        for (let i = 0; i < event.dataTransfer.files.length; i++) {
            handler(file);
        }
    }
}

function handlePluginJar(file) {
    new JSZip().loadAsync(file)
        .then(function(zip) {
            zip.file("plugin.yml").async("string").then(function(plugintext) {
                try {
                    let nativeObject = YAML.parse(plugintext);
                    console.log(nativeObject);

                    let output = document.getElementById("plugin_upload_output");
                    output.innerText = "Plugin name: " + nativeObject["name"];
                    if (nativeObject.commands !== undefined) {
                        for (let commandName of Object.keys(nativeObject.commands)) {
                            output.innerText += "\n" + commandName;
                        }

                    }
                } catch (err) {
                    let output = document.getElementById("plugin_upload_output");
                    output.innerText = err.message + " on line " + err.parsedLine + ": " + err.snippet;
                }

            });
        }, function() {
            alert("Not a valid zip file");
        });
}

function mkSuccess(message) {
    const outputElem = document.createElement("div");
    const outputHeading = document.createElement("h3");
    const outputText = document.createElement("p");

    outputElem.classList.add("example");
    outputHeading.innerText = "Success!";
    outputText.innerText = message;
    outputElem.appendChild(outputHeading);
    outputElem.appendChild(outputText);
    return outputElem;
}

function mkErr(message) {
    const outputElem = document.createElement("div");
    const outputHeading = document.createElement("h3");
    const outputText = document.createElement("p");

    outputElem.classList.add("warning");
    outputHeading.innerText = "Error";
    outputText.innerText = message;
    outputElem.appendChild(outputHeading);
    outputElem.appendChild(outputText);
    return outputElem;
}

function handleConfigFile(file) {
    const output = document.getElementById("config_upload_output");

    while (output.firstChild) {
        output.firstChild.remove();
    }

    const reader = new FileReader();
    reader.onload = function(readFileEvent) {
        const configYAML = readFileEvent.target.result.replaceAll("\r", "");
        try {
            YAML.parse(configYAML);
            output.appendChild(mkSuccess("Config is all good!"));
        } catch (err) {
            let commentCount = 0;
            for (const line of configYAML.split("\n")) {
                if (line.startsWith("#")) {
                    commentCount++;
                }
                if (line === err.snippet) {
                    break;
                }
            }
            const errorMsg = mkErr(err.message + " Error on line " + (err.parsedLine + commentCount + 1) + ": " + err.snippet + "\n");


            const pre = document.createElement("pre");
            const code = document.createElement("code");
            code.classList.add("language-yaml");
            code.classList.add("hljs");
            code.innerHTML = configYAML;

            pre.appendChild(code);
            errorMsg.appendChild(pre);

            output.appendChild(errorMsg);
            hljs.highlightAll();

            let count = 0;
            for (const line of code.children) {
                if (line.innerText === err.snippet) {
                    line.style.backgroundColor = "var(--warning-bg)";
                    break;
                }
                count++;
            }
        }
    };
    reader.onerror = () => {
        output.appendChild(mkErr("Failed to read config file, try again."));
    };
    reader.readAsText(file);
}

function pluginDropHandler(event) {
    handleDrop(event, handlePluginJar);
}

function configDropHandler(event) {
    handleDrop(event, handleConfigFile);
}

function pluginDragHandler(event) {
    event.preventDefault();
    document.getElementById("plugin_upload_text").innerText = "Drop the file here!";
}

function configDragHandler(event) {
    event.preventDefault();
    document.getElementById("config_upload_text").innerText = "Drop the file here!";
}