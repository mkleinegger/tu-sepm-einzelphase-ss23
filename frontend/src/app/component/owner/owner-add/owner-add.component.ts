import { Component } from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Owner} from 'src/app/dto/owner';
import {OwnerService} from 'src/app/service/owner.service';

@Component({
  selector: 'app-owner-add',
  templateUrl: './owner-add.component.html',
  styleUrls: ['./owner-add.component.scss']
})

export class OwnerAddComponent {

  owner: Owner = {
    firstName: '',
    lastName: '',
    email: undefined,
  };

  constructor(
    private service: OwnerService,
    private router: Router,
    private notification: ToastrService,
  ) {
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.owner);
    if (form.valid) {
      let observable: Observable<Owner>;

      observable = this.service.create(this.owner);
      observable.subscribe({
        next: data => {
          this.notification.success(`Owner ${this.owner.firstName} ${this.owner.lastName} successfully created.`);
          this.router.navigate(['/owners']);
        },
        error: error => {
          console.error('Error creating horse', error);
          // TODO show an error message to the user. Include and sensibly present the info from the backend!
        }
      });
    }
  }

}
