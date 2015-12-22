package ua.bolt;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InputFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.apache.commons.io.FileUtils;
import ua.bolt.model.CutImageResult;

import java.io.File;
import java.util.List;

/**
 * Created by kbelentsov on 22.12.2015.
 */
public class UpdateProcessor {

    private static final String MIME_TYPE = "application/zip";
    private static final String TMP_DIR = "C:\\tmp\\";

    private TelegramBot api;
    private ImageCutter cutter;

    public UpdateProcessor(TelegramBot api) {
        this.api = api;
        this.cutter = new ImageCutter();
    }

    public void processPhoto(Update update) {
        PhotoSize[] photoSizes = update.message().photo();
        PhotoSize photo = photoSizes[photoSizes.length - 1];

        String updateId = String.valueOf(update.updateId());
        String updateDir = TMP_DIR + updateId;

        GetFileResponse file = api.getFile(photo.fileId());
        String url = api.getFullFilePath(file.file());

        File fileOnDisc = FileUtil.getFileFromWeb(updateDir, updateId, url);

        CutImageResult cutImageResult = cutter.cutImages(fileOnDisc, updateDir);
        List<File> cutImages = cutImageResult.getFiles();

        File zip = FileUtil.writeZipFile(updateDir, updateId, cutImages);

        Integer chatId = update.message().from().id();

        api.sendMessage(chatId, "Done!\nSliced on rows: " + cutImageResult.getRowCount() + "\nHere is your zip archive. See ya");
        api.sendDocument(chatId, new InputFile(MIME_TYPE, zip), update.message().messageId(), null);

        FileUtil.deleteDir(updateDir);
    }

    public void processHelp(Update update) {
        api.sendMessage(update.message().from().id(),
                "" +
                        "Hello dear agent!\n" +
                        "I'll help you cut banner for ingress mission set. Just send me a photo that you wish to slice =)");
    }
}