from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField, FileField)
from wtforms.validators import DataRequired, Length

class LoginForm(FlaskForm):
    email = StringField('email', validators=[DataRequired()])
    password = PasswordField('password', validators=[DataRequired()])
    remember_me = BooleanField('remember_me')

class RegisterForm(FlaskForm):
    email = StringField('email', validators=[DataRequired()])
    password = PasswordField('password', validators=[DataRequired()])
    name = StringField('name', validators=[DataRequired()])

class SendVideoForm (FlaskForm):
    file = FileField('file',validators=[DataRequired()])