package action;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/**
 * Created by polo on 26/03/2017.
 */
public class CreateScene extends CreateElementActionBase {

    @NotNull
    @Override
    protected PsiElement[] invokeDialog(Project project, PsiDirectory directory) {
        MyInputValidator validator = new MyInputValidator(project, directory);
        Messages.showInputDialog(project, "Enter a new scene name:", "New Scene Name", Messages.getQuestionIcon(), "", validator);
        return validator.getCreatedElements();
    }

    @NotNull
    @Override
    protected PsiElement[] create(String s, PsiDirectory directory) throws Exception {

        PsiDirectory sceneDirectory = createSceneDirectory(s, directory);
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(sceneDirectory.getProject());

        String activityName = s+"Activity";
        String fragmentName = s+"Fragment";
        String presenterName = s+"Presenter";
        String vmName = s+"VM";
        String viewInterfaceName = s+"View";

        PsiJavaFile activity = createJavaFile(psiFileFactory, activityName, createActivityContent(s, activityName, fragmentName));

        PsiJavaFile fragment = createJavaFile(psiFileFactory, fragmentName, createFragmentContent(s, fragmentName, presenterName, vmName, viewInterfaceName));

        PsiJavaFile presenter = createJavaFile(psiFileFactory, presenterName, createPresenterName(presenterName, vmName, viewInterfaceName));

        PsiJavaFile vm = createJavaFile(psiFileFactory, vmName, createVM(vmName));

        PsiJavaFile view = createJavaFile(psiFileFactory, viewInterfaceName, createViewInterface(viewInterfaceName, vmName));

        return new PsiElement[]{sceneDirectory.add(activity), sceneDirectory.add(fragment), sceneDirectory.add(presenter), sceneDirectory.add(vm), sceneDirectory.add(view)};
    }

    @NotNull
    private PsiDirectory createSceneDirectory(String name, PsiDirectory rootDirectory) {
        return rootDirectory.createSubdirectory(name.toLowerCase());
    }

    private PsiJavaFile createJavaFile(PsiFileFactory factory, String name, String content) {
        return (PsiJavaFile) factory.createFileFromText(name+".java", JavaFileType.INSTANCE, content);
    }

    @Language("JAVA")
    private String createActivityContent(String baseName, String activityName, String fragmentName) {
        return "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "\n" +
                "public class " + activityName + " extends Activity {\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout." + baseName.toLowerCase() + "_activity);\n" +
                "        getFragmentManager().beginTransaction().replace(R.layout.fragment_container, new " + fragmentName + "()).commit();\n" +
                "    }\n" +
                "}";
    }

    @Language("JAVA")
    private String createFragmentContent(String baseName, String fragmentName, String presenterName, String VMName, String viewName) {
        return "import android.app.Fragment;\n" +
                "import io.reactivex.Observable;\n" +
                "import javax.inject.Inject;\n" +
                "import butterknife.ButterKnife;\n" +
                "\n" +
                "public class " + fragmentName + " extends Fragment implements " + viewName + " {\n" +
                "    \n" +
                "    @Inject\n" +
                "    " + presenterName + " presenter;\n" +
                "\n" +
                "    @Override\n" +
                "    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {\n" +
                "        return inflater.inflate(R.layout." + baseName.toLowerCase() + "_fragment, container, false);\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {\n" +
                "        super.onViewCreated(view, savedInstanceState);\n" +
                "        Butterknife.bind(this);\n" +
                "    }\n" +
                "            \n" +
                "    @Override \n" +
                "    public void onStart() {\n" +
                "        super.onStart();\n" +
                "        presenter.attach(this);\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public void onStop() {\n" +
                "        super.onStop();\n" +
                "        presenter.detach();\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public Observable<Object> intent() {\n" +
                "        //TODO: bind intent\n" +
                "        return Observable.empty();\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public void render(" + VMName + " model){\n" +
                "        //TODO: render view with model\n" +
                "    } \n" +
                "    \n" +
                "}";
    }

    @Language("JAVA")
    private String createPresenterName(String presenterName, String VMName, String ViewName) {
        return "import android.app.Fragment;\n" +
                "import android.os.Bundle;\n" +
                "import android.support.annotation.Nullable;\n" +
                "import android.view.LayoutInflater;\n" +
                "import android.view.View;\n" +
                "import android.view.ViewGroup;\n" +
                "\n" +
                "import javax.inject.Inject;\n" +
                "\n" +
                "import io.reactivex.Observable;\n" +
                "\n" +
                "public class " + presenterName + " {\n" +
                "    \n" +
                "    private CompositeDisposable disposable = new CompositeDisposable();\n" +
                "    \n" +
                "    @Inject\n" +
                "    public " + presenterName + "() {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "    public void attach(" + ViewName + " view) {\n" +
                "        //TODO attach the view and register to event\n" +
                "        disposable.add(view.intent().flatMap(__ -> doIntent()).subscribe(view::render));\n" +
                "    }\n" +
                "\n" +
                "    private Observable<" + VMName + "> doIntent() {\n" +
                "        return Observable.just(" + VMName + ".error(\"Not implemented\"));\n" +
                "    }\n" +
                "    \n" +
                "    public void detach() {\n" +
                "        disposable.dispose();\n" +
                "    }\n" +
                "}";
    }

    @Language("JAVA")
    private String createViewInterface(String viewInterface, String vmName) {
        return "import io.reactivex.Observable;\n" +
                "\n" +
                "public interface " + viewInterface + " {\n" +
                "    void render(" + vmName + " model);\n" +
                "    Observable<Object> intent();\n" +
                "}";
    }

    @Language("JAVA")
    private String createVM(String vmName) {
        return "import java.util.Collections;\n" +
                "import java.util.List;\n" +
                "import com.google.auto.value.AutoValue;\n" +
                "import android.support.annotation.NonNull;\n" +
                "import android.support.annotation.Nullable;\n" +
                "\n" +
                "@AutoValue\n" +
                "public abstract class " + vmName + " {\n" +
                "    \n" +
                "    enum LoadingState{ NONE, LOADING, REFRESHING}\n" +
                "    \n" +
                "    @NonNull\n" +
                "    abstract List<String> value();\n" +
                "    \n" +
                "    @Nullable\n" +
                "    abstract String error();\n" +
                "    \n" +
                "    abstract LoadingState loadingState();\n" +
                "    \n" +
                "    public static " + vmName + " data(@NonNull List<String> value) {\n" +
                "        return new AutoValue_" + vmName + "(value, null, LoadingState.NONE);\n" +
                "    }\n" +
                "    public static " + vmName + " refreshing(@NonNull List<String> value) {\n" +
                "        return new AutoValue_" + vmName + "(value, null, LoadingState.REFRESHING);\n" +
                "    }\n" +
                "    public static " + vmName + " loading() {\n" +
                "        return new AutoValue_" + vmName + "(Collections.emptyList(), null, LoadingState.LOADING);\n" +
                "    }\n" +
                "    public static " + vmName + " error(@NonNull List<String> value, String error) {\n" +
                "        return new AutoValue_" + vmName + "(value, error, LoadingState.NONE);\n" +
                "    }\n" +
                "    public static " + vmName + " error(String error) {\n" +
                "        return new AutoValue_ " + vmName + "(Collections.emptyList(), error, LoadingState.NONE);\n" +
                "    }\n" +
                "}";
    }

    @Override
    protected String getErrorTitle() {
        return "Error";
    }

    @Override
    protected String getCommandName() {
        return "Create scene";
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return "Create scene " + s;
    }
}
