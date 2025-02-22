import asyncio
from telegram import Bot

# 替换成你的Bot HTTP API
TOKEN = '754047254:AAFRtztLq0QdbcsQ-eUC4o99hMpbp_iBDF8'
bot = Bot(token=TOKEN, base_url='http://127.0.0.1:8081/bot', base_file_url='http://127.0.0.1:8081/file/bot')

async def get_updates():
    updates = await bot.get_updates()
    print(updates)
    for update in updates:
        if update.message:
            print(f"Chat ID: {update.message.chat.id}")

async def main():
    await get_updates()

if __name__ == '__main__':
    asyncio.run(main())
