from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm, SendVideoForm


import os
import json
import requests

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = LoginForm(request.form)
        if request.method == "POST" and form.validate():
            headers = {"Content-Type": "application/json"}
            data = {"email":f"{form.email.data}", "password":f"{form.password.data}"}
            data = json.dumps(data)
            r = requests.post(f"http://{os.environ['BACKEND_REST']}:8080/rest/checkLogin", headers=headers, data=data)
            if r.status_code != 200:
                error = 'Invalid Credentials. Please try again.'
            #if form.email.data != 'admin@um.es' or form.password.data != 'admin':
            #    error = 'Invalid Credentials. Please try again.'
            else:
                response_json = json.loads(r.text)
                user = User(response_json.get("id"), response_json.get("name"), response_json.get("email"),
                            form.password.data.encode('utf-8'), response_json.get("visits"), response_json.get("token"))
                users.insert(0, user)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))

        return render_template('login.html', form=form,  error=error)

@app.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = RegisterForm(request.form)
        if request.method == "POST":
            headers = {"Content-Type": "application/json"}
            data = {"email":f"{form.email.data}", "name":f"{form.name.data}", "password":f"{form.password.data}"}
            data = json.dumps(data)
            r = requests.post(f"http://{os.environ['BACKEND_REST']}:8080/rest/register", headers=headers, data=data)
            if r.status_code != 200:
                error = 'Email or name already registered.'
            #if form.email.data != 'admin@um.es' or form.password.data != 'admin':
            #    error = 'Invalid Credentials. Please try again.'
            else:
                response_json = json.loads(r.text)
                user = User(response_json.get("id"), response_json.get("name"), response_json.get("email"),
                            form.password.data.encode('utf-8'), response_json.get("visits"), response_json.get("token"))
                users.append(user)
                login_user(user)
                return redirect(url_for('index'))

        return render_template('signup.html', form=form,  error=error)

@app.route('/profile')
@login_required
def profile():
    return render_template('profile.html')

@app.route("/uploadVideo", methods=['GET','POST'])
@login_required
def uploadvideo():
    respuesta = None
    if request.method == 'POST':
        f = request.files['file']
        files = {'file':(f.filename, f)}
        username = current_user.name
        r = requests.post(f"http://{os.environ['BACKEND_REST']}:8080/rest/users/{username}/uploadVideo", files=files)
        respuesta = r.text
        if r.status_code != 200:
                respuesta = 'No dejar el video en blanco. Inserta un video'
        return render_template('uploadvideo.html', respuesta = respuesta)

    else:
        return render_template('uploadvideo.html')

@app.route("/showvideos", methods=['GET'])
@login_required
def showvideos():
    respuesta = None
    username = current_user.name
    r = requests.get(f"http://{os.environ['BACKEND_REST']}:8080/rest/users/{username}/showVideos")
    respuesta = r.text
    response_json = json.loads(respuesta)
    listavideos = tuple(response_json.items())
    if r.status_code != 200:
        respuesta = 'No existen videos procesados para este usuario.'
    return render_template('showvideostable.html', listavideos = listavideos)

@app.route("/showfaces/<videoid>", methods=['GET'])
@login_required
def showfaces(videoid):
    respuesta = None
    username = current_user.name
    r = requests.get(f"http://{os.environ['BACKEND_REST']}:8080/rest/users/{username}/{videoid}/showFaces")
    respuesta = r.text
    response_json = json.loads(respuesta)
    listafaces = tuple(response_json.items())
    if r.status_code != 200:
        respuesta = 'No existen caras para este video.'
    return render_template('showfacestable.html', listafaces = listafaces, videoid = videoid)    

@app.route("/deletevideo/<videoid>", methods=['GET'])
@login_required
def deletevideo(videoid):
    respuesta = None
    username = current_user.name
    r = requests.get(f"http://{os.environ['BACKEND_REST']}:8080/rest/users/{username}/{videoid}/deleteVideo")
    if r.status_code != 200:
        respuesta = 'No se ha podido borrar el video.'
    
    return redirect(url_for('showvideos'))

@app.route("/deleteface/<videoid>/<faceid>", methods=['GET'])
@login_required
def deleteface(videoid, faceid):
    respuesta = None
    username = current_user.name
    r = requests.get(f"http://{os.environ['BACKEND_REST']}:8080/rest/users/{username}/{faceid}/deleteFace")
    if r.status_code != 200:
        respuesta = 'No se ha podido borrar la cara.'
    
    return redirect(url_for('showfaces', videoid=videoid))

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == user_id:
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
