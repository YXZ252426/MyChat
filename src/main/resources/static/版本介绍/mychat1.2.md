### WebSocket 消息处理

1. **接收消息**： 当 WebSocket 接收到消息时，会调用 `onmessage` 事件处理器。

   ```javascript
   socket.onmessage = function(event) {
       const data = JSON.parse(event.data);
       const messageElement = document.createElement('div');
   
       if (data.type === 'message') {
           messageElement.textContent = `${data.username}: ${data.message}`;
           messageElement.classList.add('message');
           if (data.username === currentUser) {
               messageElement.classList.add('right');
           } else {
               messageElement.classList.add('left');
           }
       } else if (data.type === 'image') {
           const img = document.createElement('img');
           img.src = data.image;
           messageElement.appendChild(img);
           messageElement.classList.add('message', 'image-message');
           if (data.username === currentUser) {
               messageElement.classList.add('right');
           } else {
               messageElement.classList.add('left');
           }
       }
   
       chat.appendChild(messageElement);
       chat.scrollTop = chat.scrollHeight; // 自动滚动到最新消息
   };
   ```

2. **发送消息**： `sendMessage` 函数用于发送文本消息。

   ```javascript
   function sendMessage() {
       const username = usernameInput.value;
       const message = messageInput.value;
   
       if (username.trim() !== '' && message.trim() !== '') {
           if (!currentUser) {
               currentUser = username;
           }
   
           const data = { type: 'message', username: username, message: message };
           socket.send(JSON.stringify(data));
           messageInput.value = '';
       }
   }
   ```

### 图片压缩和发送

1. **处理图片上传并压缩**： 当用户选择图片时，`compressAndSendImage` 函数会被调用。

   ```javascript
   function compressAndSendImage(event) {
       const file = event.target.files[0];
   
       // 限制图片大小为2MB
       const maxSize = 2 * 1024 * 1024;
       if (file.size > maxSize) {
           alert("The file is too large. Please select a file smaller than 2MB.");
           return;
       }
   
       const reader = new FileReader();
       reader.onload = function(e) {
           const img = new Image();
           img.src = e.target.result;
           img.onload = function() {
               const canvas = document.createElement('canvas');
               const max_width = 200; // 设置最大宽度
               const max_height = 200; // 设置最大高度
               let width = img.width;
               let height = img.height;
   
               // 计算宽高比
               if (width > height) {
                   if (width > max_width) {
                       height = Math.round((height * max_width) / width);
                       width = max_width;
                   }
               } else {
                   if (height > max_height) {
                       width = Math.round((width * max_height) / height);
                       height = max_height;
                   }
               }
   
               // 设置 canvas 大小
               canvas.width = width;
               canvas.height = height;
   
               // 在 canvas 上绘制图片
               const ctx = canvas.getContext('2d');
               ctx.drawImage(img, 0, 0, width, height);
   
               // 压缩图片并获取 base64 编码
               const compressedDataUrl = canvas.toDataURL('image/jpeg', 0.7); // 70% 的质量压缩
   
               // 发送压缩后的图片数据
               const username = usernameInput.value;
               if (username.trim() !== '') {
                   if (!currentUser) {
                       currentUser = username;
                   }
   
                   const data = { type: 'image', username: username, image: compressedDataUrl };
                   socket.send(JSON.stringify(data));
               }
           };
       };
       reader.readAsDataURL(file);
   }
   ```

### 解释步骤

1. **限制文件大小**：
   - 检查上传文件的大小，如果超过 2MB，则提示用户选择较小的文件。
2. **读取文件**：
   - 使用 `FileReader` 读取文件并将其转换为数据 URL（base64 编码）。
3. **创建和加载图片**：
   - 创建一个新的 `Image` 对象，并将其源设置为读取的文件数据 URL。
4. **调整图片大小**：
   - 根据最大宽度和高度调整图片尺寸，同时保持宽高比。
5. **绘制和压缩图片**：
   - 使用 `canvas` 元素绘制调整大小后的图片，并将其压缩为指定质量的 JPEG 格式。
6. **发送压缩图片**：
   - 将压缩后的图片数据（base64 编码）作为消息发送到服务器。

### 发送图片消息

在 `compressAndSendImage` 函数中，经过处理后的图片数据作为 JSON 对象通过 WebSocket 发送到服务器。

### 显示接收的消息和图片

在 `socket.onmessage` 事件处理器中，接收到的消息和图片会被解析并显示在聊天界面中。

通过这些步骤，你可以在聊天应用中实现图片的传输和显示。